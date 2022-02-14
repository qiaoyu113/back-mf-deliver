package com.mfexpress.rent.deliver.consumer.sync;


import cn.hutool.core.collection.CollUtil;
import com.mfexpress.common.domain.api.DictAggregateRootApi;
import com.mfexpress.common.domain.dto.DictDataDTO;
import com.mfexpress.common.domain.dto.DictTypeDTO;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.component.starter.mq.relation.binlog.MFMqBinlogRelation;
import com.mfexpress.component.starter.mq.relation.binlog.MFMqBinlogTableFullName;
import com.mfexpress.component.starter.tools.es.ElasticsearchTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.order.api.app.ContractAggregateRootApi;
import com.mfexpress.order.api.app.OrderAggregateRootApi;
import com.mfexpress.order.dto.data.OrderDTO;
import com.mfexpress.order.dto.data.ProductDTO;
import com.mfexpress.order.dto.qry.ReviewOrderQry;
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
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@MFMqBinlogRelation
public class SyncServiceImpl implements EsSyncHandlerI {

    /*@Resource
    private MqTools mqTools;*/

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

    /*@Resource
    private ContractAggregateRootApi contractAggregateRootApi;

    @Value("${rocketmq.listenBinlogTopic}")
    private String listenBinlogTopic;
    @Value("${rocketmq.listenEventTopic}")
    private String listenEventTopic;
    @Resource
    private DeliverMqCommand deliverMqCommand;
    @Resource
    private DeliverVehicleMqCommand deliverVehicleMqCommand;

    /*@PostConstruct
    public void init() {

        DeliverBinlogDispatch deliverBinlogDispatch = new DeliverBinlogDispatch();
        deliverBinlogDispatch.setServiceI(this);
        mqTools.addBinlogCommand(listenBinlogTopic, deliverBinlogDispatch);
        deliverMqCommand.setTopic(listenEventTopic);
        deliverMqCommand.setTags(Constants.DELIVER_ORDER_TAG);
        mqTools.add(deliverMqCommand);
        deliverVehicleMqCommand.setTopic(listenEventTopic);
        deliverVehicleMqCommand.setTags(Constants.DELIVER_VEHICLE_TAG);
        mqTools.add(deliverVehicleMqCommand);

    }*/


    @Override
    @MFMqBinlogTableFullName({"mf-deliver.deliver", "mf-deliver.serve", "mf-deliver.deliver_vehicle", "mf-deliver.recover_vehicle"})
    public boolean execOne(Map<String, String> data) {
        ServeES serveEs = new ServeES();
        String serveNo = data.get("serve_no");
        if(StringUtils.isEmpty(serveNo)){
            return false;
        }
        Result<ServeDTO> serveResult = serveAggregateRootApi.getServeDtoByServeNo(serveNo);
        if (serveResult.getData() == null) {
            return false;
        }
        ServeDTO serveDTO = serveResult.getData();
        BeanUtils.copyProperties(serveDTO, serveEs);
        serveEs.setContractNo(serveDTO.getOaContractCode());
        serveEs.setServeStatus(serveDTO.getStatus());
        serveEs.setOrderId(serveDTO.getOrderId().toString());
        serveEs.setRent(serveDTO.getRent().toString());
        serveEs.setDeposit(serveDTO.getDeposit().toString());
        serveEs.setLeaseEndDate(serveDTO.getLeaseEndDate());
        //租赁方式
        serveEs.setLeaseModelDisplay(getDictDataDtoLabelByValue(getDictDataDtoMapByDictType(Constants.DELIVER_LEASE_MODE), serveEs.getLeaseModelId().toString()));
        serveEs.setExtractVehicleTime(serveDTO.getLeaseBeginDate());

        //品牌车型描述
        Result<String> carModelResult = vehicleAggregateRootApi.getVehicleBrandTypeById(serveEs.getCarModelId());
        serveEs.setBrandModelDisplay(carModelResult.getData());
        //调用订单查询订单信息
        ReviewOrderQry reviewOrderQry = new ReviewOrderQry();
        reviewOrderQry.setId(serveEs.getOrderId());
        Result<OrderDTO> orderResult = orderAggregateRootApi.getOrderInfo(reviewOrderQry);
        if (orderResult.getCode() == 0 && orderResult.getData() != null) {
            OrderDTO order = orderResult.getData();
            if (StringUtils.isEmpty(serveEs.getContractNo())) {
                serveEs.setContractNo(order.getOaContractCode());
            }
            Result<CustomerVO> customerResult = customerAggregateRootApi.getById(order.getCustomerId());
            if (customerResult.getCode() == 0 && customerResult.getData() != null) {
                serveEs.setCustomerName(customerResult.getData().getName());
                serveEs.setCustomerPhone(customerResult.getData().getPhone());
            }
            serveEs.setExtractVehicleTime(order.getDeliveryDate());
            List<OrderCarModelVO> carModelList = new LinkedList<>();
            List<ProductDTO> productList = order.getProductList();
            if (!CollUtil.isEmpty(productList)) {
                List<Integer> modelsIdList = productList.stream().map(ProductDTO::getModelsId).collect(Collectors.toList());
                Result<Map<Integer, String>> vehicleBrandTypeResult = vehicleAggregateRootApi.getVehicleBrandTypeListById(modelsIdList);
                Map<Integer, String> brandTypeMap = vehicleBrandTypeResult.getData();
                for (ProductDTO productDTO : productList) {
                    OrderCarModelVO orderCarModelVO = new OrderCarModelVO();
                    orderCarModelVO.setBrandId(productDTO.getBrandId());
                    orderCarModelVO.setCarModelId(productDTO.getModelsId());
                    orderCarModelVO.setBrandModelDisplay(brandTypeMap.get(productDTO.getModelsId()));
                    orderCarModelVO.setNum(productDTO.getProductNum());
                    carModelList.add(orderCarModelVO);
                }
                serveEs.setCarModelVOList(carModelList);
            }
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
            serveEs.setCarServiceId(deliverDTO.getCarServiceId());
            serveEs.setUpdateTime(deliverDTO.getUpdateTime());
            serveEs.setDeliverContractStatus(deliverDTO.getDeliverContractStatus());
            serveEs.setRecoverContractStatus(deliverDTO.getRecoverContractStatus());
            serveEs.setRecoverAbnormalFlag(deliverDTO.getRecoverAbnormalFlag());

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

        return true;
    }

    @Override
    public boolean execAll() {
        Result<List<String>> serveNoResult = serveAggregateRootApi.getServeNoListAll();
        boolean flag = true;
        if (serveNoResult.getData() != null) {
            List<String> serveNoList = serveNoResult.getData();
            Map<String, String> data = new HashMap<>();
            for (String serveNo : serveNoList) {
                data.put("serve_no", serveNo);
                boolean isSuccess = execOne(data);
                if(!isSuccess){
                    flag = false;
                }
            }
        }
        return flag;
    }

    private Integer getSort(ServeES serveEs) {
        int sort = DeliverSortEnum.ZERO.getSort();
        boolean deliverFlag = serveEs.getIsCheck().equals(JudgeEnum.NO.getCode()) || serveEs.getIsInsurance().equals(JudgeEnum.NO.getCode());
        boolean recoverFlag = serveEs.getIsInsurance().equals(JudgeEnum.NO.getCode()) || serveEs.getIsDeduction().equals(JudgeEnum.NO.getCode());
        if (serveEs.getServeStatus().equals(ServeEnum.PRESELECTED.getCode()) && serveEs.getDeliverStatus().equals(DeliverEnum.IS_DELIVER.getCode()) && serveEs.getIsCheck().equals(JudgeEnum.YES.getCode())
                && serveEs.getIsInsurance().equals(JudgeEnum.YES.getCode())) {
            if(DeliverContractStatusEnum.NOSIGN.getCode() == serveEs.getDeliverContractStatus()){
                // 待发车
                sort = DeliverSortEnum.ONE.getSort();
            }else{
                // 签署中
                sort = DeliverSortEnum.TWO.getSort();
            }
        } else if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_DELIVER.getCode()) && deliverFlag) {
            // 待预选
            sort = DeliverSortEnum.THREE.getSort();
        } else if (serveEs.getServeStatus().equals(ServeEnum.PRESELECTED.getCode()) && JudgeEnum.NO.getCode().equals(serveEs.getIsCheck())) {
            // 待验车
            sort = DeliverSortEnum.FOUR.getSort();
        } else if (serveEs.getServeStatus().equals(ServeEnum.PRESELECTED.getCode()) && JudgeEnum.YES.getCode().equals(serveEs.getIsCheck())) {
            // 待投保
            sort = DeliverSortEnum.FIVE.getSort();
        } else if ((serveEs.getServeStatus().equals(ServeEnum.DELIVER.getCode()) || serveEs.getServeStatus().equals(ServeEnum.REPAIR.getCode())) && serveEs.getDeliverStatus().equals(DeliverEnum.DELIVER.getCode())) {
            // 发车已完成
            sort = DeliverSortEnum.SIX.getSort();
        } else if (serveEs.getServeStatus().equals(ServeEnum.COMPLETED.getCode())) {
            // 收车已完成
            sort = DeliverSortEnum.SIX.getSort();
        }else if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_RECOVER.getCode())
                && serveEs.getIsCheck().equals(JudgeEnum.NO.getCode())) {
            //收车中 待验车
            sort = DeliverSortEnum.ONE.getSort();
        } else if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_RECOVER.getCode()) && serveEs.getIsCheck().equals(JudgeEnum.YES.getCode())
                && serveEs.getRecoverContractStatus() == DeliverContractStatusEnum.NOSIGN.getCode()){
            // 收车中 待收车
            sort = DeliverSortEnum.TWO.getSort();
        } else if (serveEs.getDeliverStatus().equals(DeliverEnum.IS_RECOVER.getCode())
                && (serveEs.getRecoverContractStatus() == DeliverContractStatusEnum.GENERATING.getCode() || serveEs.getRecoverContractStatus() == DeliverContractStatusEnum.SIGNING.getCode())){
            // 收车中 签署中
            sort = DeliverSortEnum.THREE.getSort();
        } else if (serveEs.getServeStatus().equals(ServeEnum.RECOVER.getCode()) && DeliverEnum.RECOVER.getCode().equals(serveEs.getDeliverStatus()) && JudgeEnum.NO.getCode().equals(serveEs.getIsInsurance())) {
            //收车中  待退保
            sort = DeliverSortEnum.FOUR.getSort();
        } else if (serveEs.getServeStatus().equals(ServeEnum.RECOVER.getCode()) && DeliverEnum.RECOVER.getCode().equals(serveEs.getDeliverStatus()) && JudgeEnum.YES.getCode().equals(serveEs.getIsInsurance())) {
            //收车中  待处理违章
            sort = DeliverSortEnum.FIVE.getSort();
        }

        return sort;

    }

    private Map<String, DictDataDTO> getDictDataDtoMapByDictType(String dictType) {
        DictTypeDTO dictTypeDTO = new DictTypeDTO();
        dictTypeDTO.setDictType(dictType);
        Result<List<DictDataDTO>> dictDataResult = dictAggregateRootApi.getDictDataByType(dictTypeDTO);
        if (dictDataResult.getCode() == 0) {
            List<DictDataDTO> dictDataDTOList = dictDataResult.getData();
            if (dictDataDTOList == null || dictDataDTOList.isEmpty()) {
                return new HashMap<>(16);
            }
            Map<String, DictDataDTO> dictDataDTOMap = dictDataDTOList.stream().collect(Collectors.toMap(DictDataDTO::getDictValue, Function.identity(), (key1, key2) -> key1));
            return dictDataDTOMap;
        }
        return null;
    }

    /**
     * 字典值
     */
    private String getDictDataDtoLabelByValue(Map<String, DictDataDTO> dictDataDtoMap, String value) {
        if (dictDataDtoMap == null) {
            return "";
        }

        DictDataDTO dictDataDTO = dictDataDtoMap.get(value);
        if (dictDataDTO != null) {
            return dictDataDTO.getDictLabel();
        }
        return "";
    }
}
