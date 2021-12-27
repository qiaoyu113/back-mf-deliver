package com.mfexpress.rent.deliver.elecHandoverContract.executor.qry;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.json.JSONUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.es.ElasticsearchTools;
import com.mfexpress.order.api.app.OrderAggregateRootApi;
import com.mfexpress.order.dto.data.OrderDTO;
import com.mfexpress.order.dto.data.ProductDTO;
import com.mfexpress.order.dto.qry.ReviewOrderQry;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.OrderCarModelVO;
import com.mfexpress.rent.deliver.dto.data.Page;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractListQry;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.DeliverContractListVO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.ElecContractWithServesVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeVO;
import com.mfexpress.rent.deliver.dto.es.ServeES;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.utils.Utils;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ContractListQryExe {

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource
    private OrderAggregateRootApi orderAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private ElasticsearchTools elasticsearchTools;

    public DeliverContractListVO execute(ContractListQry qry, TokenInfo tokenInfo) {
        // 订单信息补全
        OrderDTO orderDTO = getOrderDTO(qry.getOrderId());
        DeliverContractListVO deliverContractListVO = new DeliverContractListVO();
        deliverContractListVO.setOrderId(orderDTO.getOrderId().toString());
        deliverContractListVO.setCustomerId(orderDTO.getCustomerId());
        // 这里的一般信息查询失败不抛异常
        CustomerVO customerVO = customerAggregateRootApi.getById(orderDTO.getCustomerId()).getData();
        deliverContractListVO.setCustomerName(customerVO == null ? "" : customerVO.getName());
        deliverContractListVO.setContractNo(orderDTO.getContractCode());
        deliverContractListVO.setExtractVehicleTime(orderDTO.getDeliveryDate());

        // 补充车型信息
        List<ProductDTO> productList = orderDTO.getProductList();
        List<OrderCarModelVO> carModelList = getCarModelList(productList);
        deliverContractListVO.setCarModelVOList(carModelList);

        // 补充电子交接合同信息,单个电子交接合同下会存在多个交付单
        PagePagination<ElecContractDTO> pagePagination = contractAggregateRootApi.getPageContractDTOSByQry(qry);
        List<ElecContractWithServesVO> contractWithServesVOS = getContractWithServesVOS(pagePagination);
        deliverContractListVO.setContractWithServesVOS(contractWithServesVOS);

        // 补充分页信息
        Page page = Page.builder().nowPage(Integer.valueOf(pagePagination.getPage().getNowPage()))
                .pages(Integer.valueOf(pagePagination.getPage().getPages())).total(pagePagination.getPagination().getTotal()).build();
        deliverContractListVO.setPage(page);
        return deliverContractListVO;
    }

    private List<ElecContractWithServesVO> getContractWithServesVOS(PagePagination<ElecContractDTO> pagePagination) {
        // 收集所有合同下的交付单，一次性从es查询
        List<String> allDeliverNos = new ArrayList<>();
        List<ElecContractDTO> elecContractDTOS = pagePagination.getList();
        List<ElecContractWithServesVO> contractWithServesVOS = elecContractDTOS.stream().map(contractDTO -> {
            ElecContractWithServesVO contractWithServesVO = new ElecContractWithServesVO();
            contractWithServesVO.setElecContractId(contractDTO.getContractId().toString());
            contractWithServesVO.setElecContractNo(contractDTO.getContractForeignNo());
            contractWithServesVO.setElecContractStatus(contractDTO.getStatus());
            contractWithServesVO.setElecContractFailureReason(contractDTO.getFailureReason());

            List<String> deliverNos = JSONUtil.toList(contractDTO.getDeliverNos(), String.class);
            contractWithServesVO.setDeliverNos(deliverNos);
            allDeliverNos.addAll(deliverNos);

            contractWithServesVO.setServeVOList(new ArrayList<>());
            return contractWithServesVO;
        }).collect(Collectors.toList());

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termsQuery("deliverNo.keyword", allDeliverNos));
        FieldSortBuilder updateTimeSortBuilders = SortBuilders.fieldSort("updateTime").unmappedType("date").order(SortOrder.DESC);
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        fieldSortBuilderList.add(updateTimeSortBuilders);
        Map<String, Object> map = elasticsearchTools.searchByQuerySort(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX),
                DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), 0, 0,
                boolQueryBuilder, fieldSortBuilderList);
        List<ServeVO> serveVoList = new LinkedList<>();
        List<Map<String, Object>> datas = (List<Map<String, Object>>) map.get("data");
        for (Map<String, Object> serveMap : datas) {
            ServeVO serveVO = new ServeVO();
            ServeES serveEs = BeanUtil.mapToBean(serveMap, ServeES.class, false, new CopyOptions());
            BeanUtil.copyProperties(serveEs, serveVO);
            serveVoList.add(serveVO);
        }
        Map<String, ServeVO> serveVOMap = datas.stream().map(data -> {
            ServeVO serveVO = new ServeVO();
            ServeES serveEs = BeanUtil.mapToBean(data, ServeES.class, false, new CopyOptions());
            BeanUtil.copyProperties(serveEs, serveVO);
            return serveVO;
        }).collect(Collectors.toMap(ServeVO::getDeliverNo, Function.identity(), (v1, v2) -> v1));

        contractWithServesVOS.forEach(contractWithServesVO -> {
            List<String> deliverNos = contractWithServesVO.getDeliverNos();
            deliverNos.forEach(deliverNo -> {
                contractWithServesVO.getServeVOList().add(serveVOMap.get(deliverNo));
            });
        });
        return contractWithServesVOS;
    }

    private List<OrderCarModelVO> getCarModelList(List<ProductDTO> productList) {
        List<OrderCarModelVO> carModelList = new LinkedList<>();
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
        return carModelList;
    }

    private OrderDTO getOrderDTO(String orderId) {
        ReviewOrderQry orderQry = new ReviewOrderQry();
        orderQry.setId(orderId);
        Result<OrderDTO> orderDTOResult = orderAggregateRootApi.getOrderInfo(orderQry);
        if(!ResultErrorEnum.SUCCESSED.getCode().equals(orderDTOResult.getCode()) || null == orderDTOResult.getData()){
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "订单信息查询失败");
        }
        return orderDTOResult.getData();
    }

}
