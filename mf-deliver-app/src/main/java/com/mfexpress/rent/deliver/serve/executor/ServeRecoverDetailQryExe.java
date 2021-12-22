package com.mfexpress.rent.deliver.serve.executor;

import cn.hutool.json.JSONUtil;
import com.mfexpress.common.domain.api.DictAggregateRootApi;
import com.mfexpress.common.domain.dto.DictDataDTO;
import com.mfexpress.common.domain.dto.DictTypeDTO;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.order.api.app.OrderAggregateRootApi;
import com.mfexpress.order.dto.data.OrderDTO;
import com.mfexpress.order.dto.qry.ReviewOrderQry;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.*;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleVO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverImgInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecDocDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.ElecHandoverDocVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleVO;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.WarehouseAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.warehouse.WarehouseDto;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ServeRecoverDetailQryExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private OrderAggregateRootApi orderAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Resource
    private WarehouseAggregateRootApi warehouseAggregateRootApi;

    @Resource
    private DictAggregateRootApi dictAggregateRootApi;

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    public ServeRecoverDetailVO execute(ServeQryCmd cmd) {
        String serveNo = cmd.getServeNo();
        // 数据查询----start
        Result<ServeDTO> serveDtoResult = serveAggregateRootApi.getServeDtoByServeNo(serveNo);
        if(!DeliverUtils.resultDataCheck(serveDtoResult)){
            // commonException应封到base中
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
        ServeDTO serveDTO = serveDtoResult.getData();

        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(serveNo);
        if(!DeliverUtils.resultDataCheck(deliverDTOResult)){
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "交付单信息查询失败");
        }
        DeliverDTO deliverDTO = deliverDTOResult.getData();

        Result<RecoverVehicleDTO> recoverVehicleDTOResult = recoverVehicleAggregateRootApi.getRecoverVehicleDtoByDeliverNo(deliverDTO.getDeliverNo());
        if (!DeliverUtils.resultDataCheck(recoverVehicleDTOResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "收车单信息查询失败");
        }
        RecoverVehicleDTO recoverVehicleDTO = recoverVehicleDTOResult.getData();
        // 数据查询----end

        // 数据拼装----start
        ServeRecoverDetailVO serveRecoverDetailVO = new ServeRecoverDetailVO();
        serveRecoverDetailVO.setServeNo(serveNo);
        // serve的status属性为2或5，deliver的deliver_status属性为3，状态为待验车
        // 代验车需要补充订单/客户信息和车辆信息
        if ((ServeEnum.DELIVER.getCode().equals(serveDTO.getStatus()) || ServeEnum.REPAIR.getCode().equals(serveDTO.getStatus())) && DeliverEnum.IS_RECOVER.getCode().equals(deliverDTO.getDeliverStatus())) {
            serveRecoverDetailVO.setOrderVO(getOrderVO(serveDTO));
            serveRecoverDetailVO.setVehicleVO(getVehicleVO(serveDTO, deliverDTO, recoverVehicleDTO));
        } else if (ServeEnum.RECOVER.getCode().equals(serveDTO.getStatus()) && DeliverEnum.RECOVER.getCode().equals(deliverDTO.getDeliverStatus())) {
            serveRecoverDetailVO.setOrderVO(getOrderVO(serveDTO));
            serveRecoverDetailVO.setVehicleVO(getVehicleVO(serveDTO, deliverDTO, recoverVehicleDTO));
            if(JudgeEnum.YES.getCode().equals(deliverDTO.getIsCheck())){
                // serve的status属性为3，deliver的deliver_status属性为4，is_check属性为1，状态为待收车
                // 待收车需补充验车信息
                serveRecoverDetailVO.setVehicleValidationVO(getVehicleValidationVO());
            }
            if(DeliverContractStatusEnum.SIGNING.getCode() == deliverDTO.getRecoverContractStatus()){
                // serve的status属性为3，deliver的deliver_status属性为4，recover_contract_status 属性为1，状态为签署中
                // 需补充收车单信息
                // 从合同中取出收车单信息
                serveRecoverDetailVO.setRecoverVehicleVO(getRecoverVehicleVOFromContract(deliverDTO));
            }
            if(DeliverContractStatusEnum.COMPLETED.getCode() == deliverDTO.getRecoverContractStatus()){
                // serve的status属性为3，deliver的deliver_status属性为4，recover_contract_status 属性为1，状态为待退保
                // 需补充收车单信息
                serveRecoverDetailVO.setRecoverVehicleVO(getRecoverVehicleVO(recoverVehicleDTO));
                if(RecoverVehicleType.NORMAL.getCode() == deliverDTO.getRecoverAbnormalFlag()){
                    // 正常收车补充电子交接单信息
                    serveRecoverDetailVO.setElecHandoverDocVO(getElecHandoverDocVO(deliverDTO));
                }else{
                    // 异常收车设置异常收车标志位为真
                    serveRecoverDetailVO.setRecoverAbnormalFlag(RecoverVehicleType.ABNORMAL.getCode());
                }
            }
            if(JudgeEnum.YES.getCode().equals(deliverDTO.getIsInsurance())){
                // serve的status属性为3，deliver的deliver_status属性为4，is_insurance属性为1，状态为待处理违章
                // 待处理违章需补充保险信息
                serveRecoverDetailVO.setVehicleInsuranceVO(getVehicleInsuranceVO(deliverDTO));
            }
        } else if (ServeEnum.COMPLETED.getCode().equals(serveDTO.getStatus()) && DeliverEnum.RECOVER.getCode().equals(deliverDTO.getDeliverStatus())) {
            // serve的属性为4，deliver的deliver_status属性为4，is_check属性为1，is_insurance属性为1，is_deduction属性为1，状态为已完成
            // 可以只判断status、deliver_status，属性不用判断is_xxx属性
            // 已完成需补充违章信息
            serveRecoverDetailVO.setOrderVO(getOrderVO(serveDTO));
            serveRecoverDetailVO.setVehicleVO(getVehicleVO(serveDTO, deliverDTO, recoverVehicleDTO));
            serveRecoverDetailVO.setVehicleValidationVO(getVehicleValidationVO());
            serveRecoverDetailVO.setRecoverVehicleVO(getRecoverVehicleVO(recoverVehicleDTO));
            serveRecoverDetailVO.setVehicleInsuranceVO(getVehicleInsuranceVO(deliverDTO));
            serveRecoverDetailVO.setViolationInfoVO(getViolationInfoVO(deliverDTO));
        } else {
            throw new CommonException(400005, "数据状态异常");
        }
        // 数据拼装----end

        return serveRecoverDetailVO;
    }

    private ElecHandoverDocVO getElecHandoverDocVO(DeliverDTO deliverDTO) {
        Result<ElecDocDTO> docDTOResult = contractAggregateRootApi.getDocDTOByDeliverNoAndDeliverType(deliverDTO.getDeliverNo(), DeliverTypeEnum.RECOVER.getCode());
        ElecDocDTO docDTO = ResultDataUtils.getInstance(docDTOResult).getDataOrException();
        ElecHandoverDocVO elecHandoverDocVO = new ElecHandoverDocVO();
        elecHandoverDocVO.setFileUrl(docDTO.getFileUrl());
        return elecHandoverDocVO;
    }

    private RecoverVehicleVO getRecoverVehicleVOFromContract(DeliverDTO deliverDTO) {
        Result<ElecContractDTO> contractDTOSResult = contractAggregateRootApi.getContractDTOByDeliverNoAndDeliverType(deliverDTO.getDeliverNo(), DeliverTypeEnum.RECOVER.getCode());
        ElecContractDTO contractDTO = ResultDataUtils.getInstance(contractDTOSResult).getDataOrException();

        RecoverVehicleVO recoverVehicleVO = new RecoverVehicleVO();
        BeanUtils.copyProperties(contractDTO, recoverVehicleVO);
        recoverVehicleVO.setDamageFee(contractDTO.getRecoverDamageFee());
        recoverVehicleVO.setParkFee(contractDTO.getRecoverParkFee());
        recoverVehicleVO.setElecContractId(contractDTO.getContractId().toString());
        recoverVehicleVO.setElecContractStatus(contractDTO.getStatus());
        recoverVehicleVO.setElecContractFailureReason(contractDTO.getFailureReason());
        recoverVehicleVO.setRecoverTypeDisplay(RecoverVehicleType.getEnumValue(deliverDTO.getRecoverAbnormalFlag()));
        Result<WarehouseDto> wareHouseResult = warehouseAggregateRootApi.getWarehouseById(contractDTO.getRecoverWareHouseId());
        if (wareHouseResult.getData() != null) {
            recoverVehicleVO.setWareHouseDisplay(wareHouseResult.getData().getName());
        }
        List<DeliverImgInfo> deliverImgInfos = JSONUtil.toList(contractDTO.getPlateNumberWithImgs(), DeliverImgInfo.class);
        if(deliverImgInfos.isEmpty()){
            return null;
        }
        recoverVehicleVO.setImgUrl(deliverImgInfos.get(0).getImgUrl());
        return recoverVehicleVO;
    }

    public ViolationInfoVO getViolationInfoVO(DeliverDTO deliverDTO){
        ViolationInfoVO violationInfoVO = new ViolationInfoVO();
        BeanUtils.copyProperties(deliverDTO, violationInfoVO);
        violationInfoVO.setDeductionHandle(deliverDTO.getDeductionHandel());
        violationInfoVO.setDeductionHandleDisplay(getDictDataDtoLabelByValue(getDictDataDtoMapByDictType(Constants.TRAFFIC_PECCANCY_DEALING_METHOD), deliverDTO.getDeductionHandel().toString()));
        return violationInfoVO;
    }

    public VehicleInsuranceVO getVehicleInsuranceVO(DeliverDTO deliverDTO){
        VehicleInsuranceVO vehicleInsuranceVO = new VehicleInsuranceVO();
        if(0 == deliverDTO.getInsuranceRemark()){
            // 正常退保 补全退保时间
            vehicleInsuranceVO.setIsInsurance(JudgeEnum.YES.getCode());
            vehicleInsuranceVO.setIsInsuranceDisplay(JudgeEnum.YES.getName());
            vehicleInsuranceVO.setEndTime(deliverDTO.getInsuranceEndTime());
        }else{
            // 暂不退保 补全原因
            vehicleInsuranceVO.setIsInsurance(JudgeEnum.NO.getCode());
            vehicleInsuranceVO.setIsInsuranceDisplay(JudgeEnum.NO.getName());
            vehicleInsuranceVO.setInsuranceRemark(deliverDTO.getInsuranceRemark());
            vehicleInsuranceVO.setInsuranceRemarkDisplay(getDictDataDtoLabelByValue(getDictDataDtoMapByDictType(Constants.REASONS_FOR_NOT_INSURANCE_RETURN), deliverDTO.getInsuranceRemark().toString()));
        }
        return vehicleInsuranceVO;
    }

    public RecoverVehicleVO getRecoverVehicleVO(RecoverVehicleDTO recoverVehicleDTO){
        RecoverVehicleVO recoverVehicleVO = new RecoverVehicleVO();
        BeanUtils.copyProperties(recoverVehicleDTO, recoverVehicleVO);
        Result<WarehouseDto> wareHouseResult = warehouseAggregateRootApi.getWarehouseById(recoverVehicleDTO.getWareHouseId());
        if (wareHouseResult.getData() != null) {
            recoverVehicleVO.setWareHouseDisplay(wareHouseResult.getData().getName());
        }
        return recoverVehicleVO;
    }

    public VehicleValidationVO getVehicleValidationVO() {
        VehicleValidationVO vehicleValidationVO = new VehicleValidationVO();
        vehicleValidationVO.setCheckFlag(true);
        return vehicleValidationVO;
    }

    public VehicleVO getVehicleVO(ServeDTO serveDTO, DeliverDTO deliverDTO, RecoverVehicleDTO recoverVehicleDTO) {
        Result<String> carModelResult = vehicleAggregateRootApi.getVehicleBrandTypeById(serveDTO.getCarModelId());

        Result<DeliverVehicleDTO> deliverVehicleDTOResult = deliverVehicleAggregateRootApi.getDeliverVehicleDto(deliverDTO.getDeliverNo());
        if (!DeliverUtils.resultDataCheck(deliverVehicleDTOResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "发车单信息查询失败");
        }
        DeliverVehicleDTO deliverVehicleDTO = deliverVehicleDTOResult.getData();

        VehicleVO vehicleVO = new VehicleVO();
        vehicleVO.setCarNum(deliverDTO.getCarNum());
        //vehicleVO.setBrandId();
        vehicleVO.setBrandModelDisplay(carModelResult.getData());
        vehicleVO.setVin(deliverDTO.getFrameNum());
        vehicleVO.setDeliverVehicleTime(deliverVehicleDTO.getDeliverVehicleTime());
        vehicleVO.setExpectRecoverTime(recoverVehicleDTO.getExpectRecoverTime());
        return vehicleVO;
    }

    public OrderVO getOrderVO(ServeDTO serveDTO) {
        ReviewOrderQry reviewOrderQry = new ReviewOrderQry();
        reviewOrderQry.setId(String.valueOf(serveDTO.getOrderId()));
        Result<OrderDTO> orderResult = orderAggregateRootApi.getOrderInfo(reviewOrderQry);
        if (!DeliverUtils.resultDataCheck(orderResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "订单信息查询失败");
        }
        OrderDTO orderDTO = orderResult.getData();
        OrderVO orderVO = new OrderVO();
        orderVO.setContractNo(orderDTO.getContractCode());
        orderVO.setDeliveryDate(orderDTO.getDeliveryDate());
        Result<CustomerVO> customerResult = customerAggregateRootApi.getById(orderDTO.getCustomerId());
        if (!DeliverUtils.resultDataCheck(customerResult)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "客户信息查询失败");
        }
        orderVO.setCustomerName(customerResult.getData().getName());
        return orderVO;
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
