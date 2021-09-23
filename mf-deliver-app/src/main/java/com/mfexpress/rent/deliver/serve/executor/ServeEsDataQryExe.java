package com.mfexpress.rent.deliver.serve.executor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.ElasticsearchTools;
import com.mfexpress.order.api.app.OrderAggregateRootApi;
import com.mfexpress.order.dto.data.OrderDTO;
import com.mfexpress.order.dto.data.ProductDTO;
import com.mfexpress.order.dto.qry.ReviewOrderQry;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.dto.data.OrderCarModelVO;
import com.mfexpress.rent.deliver.dto.data.Page;
import com.mfexpress.rent.deliver.dto.data.serve.ServeListVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeVO;
import com.mfexpress.rent.deliver.dto.es.ServeES;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.utils.Utils;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.entity.Customer;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class ServeEsDataQryExe {

    @Resource
    private ElasticsearchTools elasticsearchTools;
    @Resource
    private OrderAggregateRootApi orderAggregateRootApi;
    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;
    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    public ServeListVO execute(String orderId, QueryBuilder boolQueryBuilder, int nowPage, int limit, List<FieldSortBuilder> fieldSortBuilderList) {
        ServeListVO serveListVO = new ServeListVO();
        int start = (nowPage - 1) * limit;
        Map<String, Object> map = elasticsearchTools.searchByQuerySort(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX),
                Utils.getEnvVariable(Constants.ES_DELIVER_INDEX), start, limit,
                boolQueryBuilder, fieldSortBuilderList);
        List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
        List<ServeVO> serveVoList = new LinkedList<>();
        for (Map<String, Object> serveMap : data) {
            ServeVO serveVO = new ServeVO();
            ServeES serveEs = BeanUtil.mapToBean(serveMap, ServeES.class, false, new CopyOptions());
            BeanUtil.copyProperties(serveEs, serveVO);
            serveVoList.add(serveVO);
        }
        if (data.size() > 0) {
            Map<String, Object> mapExample = data.get(0);
            ServeES serveEsExample = BeanUtil.mapToBean(mapExample, ServeES.class, false, new CopyOptions());
            serveListVO.setOrderId(serveEsExample.getOrderId());
            serveListVO.setCarModelVOList(serveEsExample.getCarModelVOList());
            serveListVO.setCustomerName(serveEsExample.getCustomerName());
            serveListVO.setContractNo(serveEsExample.getContractNo());
            serveListVO.setExtractVehicleTime(serveEsExample.getExtractVehicleTime());
            serveListVO.setCustomerId(serveEsExample.getCustomerId());
        } else {
            //todo es查询订单信息
            ReviewOrderQry reviewOrderQry = new ReviewOrderQry();
            reviewOrderQry.setId(orderId);
            Result<?> orderResult = orderAggregateRootApi.getOrderInfo(reviewOrderQry);
            if (orderResult.getCode() == 0 && orderResult.getData() != null) {
                OrderDTO order = (OrderDTO) orderResult.getData();
                serveListVO.setOrderId(orderId);
                serveListVO.setContractNo(order.getContractCode());
                serveListVO.setCustomerId(order.getCustomerId());
                Result<Customer> customerResult = customerAggregateRootApi.getCustomerById(order.getCustomerId());
                if (customerResult.getCode() == 0 && customerResult.getData() != null) {
                    serveListVO.setCustomerName(customerResult.getData().getName());
                }
                serveListVO.setExtractVehicleTime(order.getDeliveryDate());
                List<OrderCarModelVO> carModelList = new LinkedList<>();
                List<ProductDTO> productList = order.getProductList();
               // List<Integer> modelsIdList = productList.stream().map(ProductDTO::getModelsId).collect(Collectors.toList());
               // Result<Map<Integer, String>> brandTypeResult = vehicleAggregateRootApi.getVehicleBrandTypeListById(modelsIdList);
               // Map<Integer, String> brandTypeMap = brandTypeResult.getData();
                for (ProductDTO productDTO : productList) {
                    OrderCarModelVO orderCarModelVO = new OrderCarModelVO();
                    orderCarModelVO.setBrandId(productDTO.getBrandId());
                    orderCarModelVO.setCarModelId(productDTO.getModelsId());
                    Result<String> brandTypeResult = vehicleAggregateRootApi.getVehicleBrandTypeById(productDTO.getModelsId());
                    orderCarModelVO.setBrandModelDisplay(brandTypeResult.getData());
                    //orderCarModelVO.setBrandModelDisplay(brandTypeMap.get(productDTO.getModelsId()));
                    orderCarModelVO.setNum(productDTO.getProductNum());
                    carModelList.add(orderCarModelVO);
                }
                serveListVO.setCarModelVOList(carModelList);
            }
        }

        long total = (long) map.get("total");
        BigDecimal bigDecimalTotal = new BigDecimal(total);
        BigDecimal bigDecimalLimit = new BigDecimal(limit);
        BigDecimal pages = BigDecimal.ZERO;
        if (limit > 0) {
            pages = bigDecimalTotal.divide(bigDecimalLimit, BigDecimal.ROUND_UP);
        }
        Page page = Page.builder().nowPage(nowPage).pages(pages.intValue()).total((int) total).build();
        serveListVO.setPage(page);
        serveListVO.setServeVOList(serveVoList);
        return serveListVO;

    }
}
