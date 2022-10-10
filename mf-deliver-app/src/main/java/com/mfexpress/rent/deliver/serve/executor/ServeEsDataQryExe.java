package com.mfexpress.rent.deliver.serve.executor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.ElasticsearchTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.order.api.app.ContractAggregateRootApi;
import com.mfexpress.order.api.app.OrderAggregateRootApi;
import com.mfexpress.order.dto.data.CommodityDTO;
import com.mfexpress.order.dto.data.InsuranceInfoDTO;
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
import com.mfexpress.rent.deliver.utils.ServeDictDataUtil;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Resource
    private ContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private BeanFactory beanFactory;

    public ServeListVO execute(String orderId, QueryBuilder boolQueryBuilder, int nowPage, int limit, List<FieldSortBuilder> fieldSortBuilderList) {
        ServeDictDataUtil.initDictData(beanFactory);

        List<FieldSortBuilder> sortBuilderList = new LinkedList<>();
        FieldSortBuilder scoreSortBuilder = SortBuilders.fieldSort("_score").order(SortOrder.DESC);
        sortBuilderList.add(scoreSortBuilder);
        sortBuilderList.addAll(fieldSortBuilderList);

        ServeListVO serveListVO = new ServeListVO();
        int start = (nowPage - 1) * limit;
        Map<String, Object> map = elasticsearchTools.searchByQuerySort(DeliverUtils.getEnvVariable(Constants.ES_SERVE_INDEX),
                Constants.ES_SERVE_TYPE, start, limit, boolQueryBuilder, sortBuilderList);
        List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
        List<ServeVO> serveVoList = new LinkedList<>();
        for (Map<String, Object> serveMap : data) {
            ServeVO serveVO = new ServeVO();
            ServeES serveEs = BeanUtil.mapToBean(serveMap, ServeES.class, false, new CopyOptions());
            BeanUtil.copyProperties(serveEs, serveVO);
            if (null != serveVO.getVehicleBusinessMode()) {
                serveVO.setVehicleBusinessModeDisplay(ServeDictDataUtil.vehicleBusinessModeMap.get(serveVO.getVehicleBusinessMode().toString()));
            }
            serveVoList.add(serveVO);
        }
        ReviewOrderQry reviewOrderQry = new ReviewOrderQry();
        reviewOrderQry.setId(orderId);
        Result<?> orderResult = orderAggregateRootApi.getOrderInfo(reviewOrderQry);
        if (orderResult.getCode() == 0 && orderResult.getData() != null) {
            OrderDTO order = (OrderDTO) orderResult.getData();
            // 合同编号以订单中的信息为准
            serveListVO.setContractNo(order.getOaContractCode());
        }
        if (data.size() > 0) {
            Map<String, Object> mapExample = data.get(0);
            ServeES serveEsExample = BeanUtil.mapToBean(mapExample, ServeES.class, false, new CopyOptions());
            serveListVO.setOrderId(serveEsExample.getOrderId());
            serveListVO.setCarModelVOList(serveEsExample.getCarModelVOList());
            serveListVO.setCustomerName(serveEsExample.getCustomerName());
            serveListVO.setExtractVehicleTime(serveEsExample.getExtractVehicleTime());
            serveListVO.setCustomerId(serveEsExample.getCustomerId());
        } else {
            //todo es查询订单信息
            if (orderResult.getCode() == 0 && orderResult.getData() != null) {
                OrderDTO order = (OrderDTO) orderResult.getData();
                serveListVO.setOrderId(orderId);
                serveListVO.setCustomerId(order.getCustomerId());
                Result<CustomerVO> customerResult = customerAggregateRootApi.getById(order.getCustomerId());
                if (customerResult.getCode() == 0 && customerResult.getData() != null) {
                    serveListVO.setCustomerName(customerResult.getData().getName());
                }

                serveListVO.setExtractVehicleTime(order.getDeliveryDate());
                List<OrderCarModelVO> carModelList = new LinkedList<>();
                List<ProductDTO> productList = order.getProductList();
                List<Integer> modelsIdList = productList.stream().map(ProductDTO::getModelsId).collect(Collectors.toList());
                Result<Map<Integer, String>> brandTypeResult = vehicleAggregateRootApi.getVehicleBrandTypeListById(modelsIdList);
                Map<Integer, String> brandTypeMap = brandTypeResult.getData();
                for (ProductDTO productDTO : productList) {
                    OrderCarModelVO orderCarModelVO = new OrderCarModelVO();
                    orderCarModelVO.setBrandId(productDTO.getBrandId());
                    orderCarModelVO.setCarModelId(productDTO.getModelsId());
                    // Result<String> brandTypeResult = vehicleAggregateRootApi.getVehicleBrandTypeById(productDTO.getModelsId());
                    //orderCarModelVO.setBrandModelDisplay(brandTypeResult.getData());
                    orderCarModelVO.setBrandModelDisplay(brandTypeMap.get(productDTO.getModelsId()));
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

    public void supplyVehicleInsureRequirement(List<ServeVO> serveVOList) {
        if (null == serveVOList || serveVOList.isEmpty()) {
            return;
        }
        List<Integer> commodityIds = serveVOList.stream().map(ServeVO::getContractCommodityId).collect(Collectors.toList());
        Result<List<CommodityDTO>> commodityListResult = contractAggregateRootApi.getCommodityListByIdList(commodityIds);
        List<CommodityDTO> commodityList = ResultDataUtils.getInstance(commodityListResult).getDataOrNull();
        if (null != commodityList) {
            Map<Integer, CommodityDTO> commodityDTOMap = commodityList.stream().collect(Collectors.toMap(CommodityDTO::getId, Function.identity(), (v1, v2) -> v1));
            for (ServeVO serveVO : serveVOList) {
                CommodityDTO commodityDTO = commodityDTOMap.get(serveVO.getContractCommodityId());
                if (null != commodityDTO) {
                    InsuranceInfoDTO insuranceInfo = commodityDTO.getInsuranceInfo();
                    if (null != insuranceInfo.getInCarPersonnelLiabilityCoverage() || null != insuranceInfo.getThirdPartyLiabilityCoverage()) {
                        // 对车辆保险状态不做要求
                        serveVO.setVehicleInsureRequirement(1);
                    } else {
                        // 只能选择交强险在保，而商业险不在保的车辆
                        serveVO.setVehicleInsureRequirement(2);
                    }
                }
            }
        }
    }

}
