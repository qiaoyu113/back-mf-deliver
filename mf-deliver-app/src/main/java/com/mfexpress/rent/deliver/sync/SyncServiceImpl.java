package com.mfexpress.rent.deliver.sync;


import com.mfexpress.common.domain.api.DictAggregateRootApi;
import com.mfexpress.common.domain.dto.DictDataDTO;
import com.mfexpress.common.domain.dto.DictTypeDTO;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.ElasticsearchTools;
import com.mfexpress.component.starter.utils.MqTools;
import com.mfexpress.order.api.app.OrderAggregateRootApi;
import com.mfexpress.order.dto.data.OrderDTO;
import com.mfexpress.order.dto.data.ProductDTO;
import com.mfexpress.order.dto.qry.ReviewOrderQry;
import com.mfexpress.rent.deliver.api.SyncServiceI;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.OrderCarModelVO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.es.ServeES;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.entity.Customer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SyncServiceImpl implements SyncServiceI {

    @Resource
    private MqTools mqTools;

    @Resource
    private ElasticsearchTools elasticsearchTools;
    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;
    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;
    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;
    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;
    @Resource
    private OrderAggregateRootApi orderAggregateRootApi;
    @Resource
    private DictAggregateRootApi dictAggregateRootApi;

    @Value("${rocketmq.listenBinlogTopic}")
    private String listenBinlogTopic;
    @Value("${rocketmq.listenOrderTopic}")
    private String listenOrderTopic;
    @Resource
    private DeliverMqCommand deliverMqCommand;
    @Resource
    private DeliverVehicleMqCommand deliverVehicleMqCommand;
    @Resource
    private DeliverUtils deliverUtils;

    @PostConstruct
    public void init() {
        DeliverBinlogDispatch deliverBinlogDispatch = new DeliverBinlogDispatch();
        deliverBinlogDispatch.setServiceI(this);
        mqTools.addBinlogCommand(listenBinlogTopic, deliverBinlogDispatch);
        deliverMqCommand.setTopic(DeliverUtils.getEnvVariable(listenOrderTopic));
        deliverMqCommand.setTags(Constants.DELIVER_ORDER_TAG);
        mqTools.add(deliverMqCommand);
        deliverVehicleMqCommand.setTopic(DeliverUtils.getEnvVariable(listenOrderTopic));
        deliverVehicleMqCommand.setTags(Constants.DELIVER_VEHICLE_TAG);
        mqTools.add(deliverVehicleMqCommand);

    }

    @Override
    public void execOne(String serveNo, String table, String type) {
        ServeES serveEs = new ServeES();
        Result<ServeDTO> serveResult = serveAggregateRootApi.getServeDtoByServeNo(serveNo);
        if (serveResult.getData() == null) {
            return;
        }
        ServeDTO serveDTO = serveResult.getData();
        BeanUtils.copyProperties(serveDTO, serveEs);
        serveEs.setServeStatus(serveDTO.getStatus());
        serveEs.setOrderId(serveDTO.getOrderId().toString());
        //租赁方式
        serveEs.setLeaseModelDisplay(getDictDataDtoLabelByValue(getDictDataDtoMapByDictType(Constants.DELIVER_LEASE_MODE), serveEs.getLeaseModelId().toString()));

        //品牌车型描述
        Result<String> carModelResult = vehicleAggregateRootApi.getVehicleBrandTypeById(serveEs.getCarModelId());
        serveEs.setBrandModelDisplay(carModelResult.getData());
        //调用订单查询订单信息
        ReviewOrderQry reviewOrderQry = new ReviewOrderQry();
        reviewOrderQry.setId(serveEs.getOrderId());
        Result<OrderDTO> orderResult = orderAggregateRootApi.getOrderInfo(reviewOrderQry);
        if (orderResult.getCode() == 0 && orderResult.getData() != null) {
            OrderDTO order = orderResult.getData();
            serveEs.setContractNo(order.getContractCode());
            Result<Customer> customerResult = customerAggregateRootApi.getCustomerById(order.getCustomerId());
            if (customerResult.getCode() == 0 && customerResult.getData() != null) {
                serveEs.setCustomerName(customerResult.getData().getName());
            }
            serveEs.setCustomerPhone(order.getConsigneeMobile());
            serveEs.setExtractVehicleTime(order.getDeliveryDate());
            List<OrderCarModelVO> carModelList = new LinkedList<>();
            List<ProductDTO> productList = order.getProductList();
            List<Integer> modelsIdList = productList.stream().map(ProductDTO::getModelsId).collect(Collectors.toList());
            Result<Map<Integer, String>> vehicleBrandTypeResult = vehicleAggregateRootApi.getVehicleBrandTypeListById(modelsIdList);
            Map<Integer, String> brandTypeMap = vehicleBrandTypeResult.getData();
            for (ProductDTO productDTO : productList) {
                OrderCarModelVO orderCarModelVO = new OrderCarModelVO();
                orderCarModelVO.setBrandId(productDTO.getBrandId());
                orderCarModelVO.setCarModelId(productDTO.getModelsId());
//                Result<String> brandTypeResult = vehicleAggregateRootApi.getVehicleBrandTypeById(productDTO.getModelsId());
//                orderCarModelVO.setBrandModelDisplay(brandTypeResult.getData());
                orderCarModelVO.setBrandModelDisplay(brandTypeMap.get(productDTO.getModelsId()));
                orderCarModelVO.setNum(productDTO.getProductNum());
                carModelList.add(orderCarModelVO);
            }
            serveEs.setCarModelVOList(carModelList);
        }


        //预选状态下存在交付单
        Result<DeliverDTO> deliverResult = deliverAggregateRootApi.getDeliverByServeNo(serveNo);
        if (deliverResult.getData() != null) {
            serveEs.setIsPreselected(ServeEnum.PRESELECTED.getCode());
            DeliverDTO deliverDTO = deliverResult.getData();
            serveEs.setDeliverNo(deliverDTO.getDeliverNo());
            serveEs.setDeliverStatus(deliverDTO.getDeliverStatus());
            serveEs.setIsInsurance(deliverDTO.getIsInsurance());
            serveEs.setIsCheck(deliverDTO.getIsCheck());
            serveEs.setIsDeduction(deliverDTO.getIsDeduction());
            serveEs.setCarId(deliverDTO.getCarId());
            serveEs.setCarNum(deliverDTO.getCarNum());
            serveEs.setFrameNum(deliverDTO.getFrameNum());
            serveEs.setMileage(deliverDTO.getMileage());
            serveEs.setVehicleAge(deliverDTO.getVehicleAge());
            serveEs.setUpdateTime(deliverDTO.getUpdateTime());

            //排序规则
            Integer sort = getSort(serveEs);
            serveEs.setSort(sort);

            Result<DeliverVehicleDTO> deliverVehicleResult = deliverVehicleAggregateRootApi.getDeliverVehicleDto(deliverDTO.getDeliverNo());
            if (deliverVehicleResult.getData() != null) {
                DeliverVehicleDTO deliverVehicleDTO = deliverVehicleResult.getData();
                serveEs.setDeliverVehicleTime(deliverVehicleDTO.getDeliverVehicleTime());
            }
            Result<RecoverVehicleDTO> recoverVehicleResult = recoverVehicleAggregateRootApi.getRecoverVehicleDtoByDeliverNo(deliverDTO.getDeliverNo());
            if (recoverVehicleResult.getData() != null) {
                RecoverVehicleDTO recoverVehicleDTO = recoverVehicleResult.getData();
                serveEs.setRecoverVehicleTime(recoverVehicleDTO.getRecoverVehicleTime());
                serveEs.setExpectRecoverTime(recoverVehicleDTO.getExpectRecoverTime());
            }
        } else {
            serveEs.setIsPreselected(JudgeEnum.NO.getCode());
            serveEs.setDeliverStatus(ServeEnum.NOT_PRESELECTED.getCode());
            serveEs.setIsCheck(0);
            serveEs.setIsInsurance(0);
            serveEs.setIsDeduction(0);
            serveEs.setSort(DeliverSortEnum.TWO.getSort());
        }
        elasticsearchTools.saveByEntity(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), serveNo, serveEs);

    }

    private Integer getSort(ServeES serveEs) {
        int sort = DeliverSortEnum.ZERO.getSort();
        boolean deliverFlag = serveEs.getIsCheck().equals(JudgeEnum.NO.getCode()) || serveEs.getIsInsurance().equals(JudgeEnum.NO.getCode());
        boolean recoverFlag = serveEs.getIsInsurance().equals(JudgeEnum.NO.getCode()) || serveEs.getIsDeduction().equals(JudgeEnum.NO.getCode());
        //待发车
        if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_DELIVER.getCode()) && serveEs.getIsCheck().equals(JudgeEnum.YES.getCode())
                && serveEs.getIsInsurance().equals(JudgeEnum.YES.getCode())) {
            sort = DeliverSortEnum.ONE.getSort();
        } else if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_DELIVER.getCode()) && deliverFlag) {
            //待验车、待投保
            sort = DeliverSortEnum.THREE.getSort();
        } else if (serveEs.getDeliverStatus().equals(DeliverEnum.DELIVER.getCode())) {
            //已发车
            sort = DeliverSortEnum.FOUR.getSort();
        } else if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_RECOVER.getCode())
                && serveEs.getIsCheck().equals(JudgeEnum.NO.getCode())) {
            //收车中 待验车
            sort = DeliverSortEnum.ONE.getSort();
        } else if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_RECOVER.getCode()) && serveEs.getIsCheck().equals(JudgeEnum.YES.getCode())
                && recoverFlag) {
            //收车中  待退保、待处理违章
            sort = DeliverSortEnum.TWO.getSort();
        } else if (serveEs.getDeliverStatus().equals(ServeEnum.COMPLETED.getCode())) {
            //已完成
            sort = DeliverSortEnum.THREE.getSort();
        }

        return sort;

    }

    private Map<String, DictDataDTO> getDictDataDtoMapByDictType(String dictType) {
        DictTypeDTO dictTypeDTO = new DictTypeDTO();
        dictTypeDTO.setDictType(dictType);
        List<DictDataDTO> dictDataDTOList = dictAggregateRootApi.getDictByType(dictTypeDTO);
        if (dictDataDTOList == null || dictDataDTOList.isEmpty()) {
            return new HashMap<>(16);
        }
        Map<String, DictDataDTO> dictDataDTOMap = dictDataDTOList.stream().collect(Collectors.toMap(DictDataDTO::getDictValue, Function.identity(), (key1, key2) -> key1));
        return dictDataDTOMap;
    }

    /**
     * 字典值
     */
    private String getDictDataDtoLabelByValue(Map<String, DictDataDTO> dictDataDtoMap, String value) {
        DictDataDTO dictDataDTO = dictDataDtoMap.get(value);
        if (dictDataDTO != null) {
            return dictDataDTO.getDictLabel();
        }
        return "";
    }
}
