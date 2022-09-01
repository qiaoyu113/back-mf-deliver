package com.mfexpress.rent.deliver.deliver.executor;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.LeaseModelEnum;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverReplaceCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverVehicleSelectCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.TipVO;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCheckCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.serve.executor.ReactiveServeCheckCmdExe;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.VehicleInsuranceAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.PolicyStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInfoDto;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInsuranceDTO;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.rent.vehicle.data.dto.vehicleinsurance.VehicleInsuranceDto;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Component
public class DeliverToReplaceExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private VehicleInsuranceAggregateRootApi vehicleInsuranceAggregateRootApi;

    @Resource
    private ReactiveServeCheckCmdExe reactiveServeCheck;

    public TipVO execute(DeliverReplaceCmd deliverReplaceCmd) {
        String serveNo = deliverReplaceCmd.getServeList().get(0);
        List<DeliverVehicleSelectCmd> deliverVehicleSelectCmdList = deliverReplaceCmd.getDeliverVehicleSelectCmd();
        DeliverVehicleSelectCmd deliverVehicleSelectCmd = deliverVehicleSelectCmdList.get(0);
        Integer vehicleId = deliverVehicleSelectCmd.getId();
        String plateNumber = deliverVehicleSelectCmd.getPlateNumber();

        // 重新激活的服务单在进行重新预选操作时需要的校验
        ReactivateServeCheckCmd reactivateServeCheckCmd = ReactivateServeCheckCmd.builder().serveNoList(Collections.singletonList(serveNo)).build();
        reactiveServeCheck.execute(reactivateServeCheckCmd);

        Result<List<VehicleInsuranceDTO>> vehicleInsuranceDTOSResult = vehicleAggregateRootApi.getVehicleInsuranceByVehicleIds(Collections.singletonList(vehicleId));
        List<VehicleInsuranceDTO> vehicleInsuranceDTOS = ResultDataUtils.getInstance(vehicleInsuranceDTOSResult).getDataOrException();
        if (null == vehicleInsuranceDTOS || vehicleInsuranceDTOS.isEmpty() || null == vehicleInsuranceDTOS.get(0)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆保险信息查询失败");
        }
        VehicleInsuranceDTO vehicleInsuranceDTO = vehicleInsuranceDTOS.get(0);
        if (null == vehicleInsuranceDTO.getCompulsoryInsuranceStatus() || null == vehicleInsuranceDTO.getCommercialInsuranceStatus()) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆".concat(deliverVehicleSelectCmd.getPlateNumber()).concat("的交强险或商业险状态异常"));
        }
        if (PolicyStatusEnum.EXPIRED.getCode() != vehicleInsuranceDTO.getCompulsoryInsuranceStatus()) {
            if (StringUtils.isEmpty(vehicleInsuranceDTO.getCompulsoryInsuranceId())) {
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆".concat(deliverVehicleSelectCmd.getPlateNumber()).concat("的交强险在保，但保单缺失"));
            }
        }
        if (PolicyStatusEnum.EXPIRED.getCode() != vehicleInsuranceDTO.getCommercialInsuranceStatus()) {
            if (StringUtils.isEmpty(vehicleInsuranceDTO.getCommercialInsuranceId())) {
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆".concat(deliverVehicleSelectCmd.getPlateNumber()).concat("的商业险在保，但保单缺失"));
            }
        }

        // 进行保险校验
        if (2 == deliverReplaceCmd.getVehicleInsureRequirement()) {
            if (PolicyStatusEnum.EFFECT.getCode() != vehicleInsuranceDTO.getCompulsoryInsuranceStatus()) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "仅能选择交强险在保车辆。");
            } else if (PolicyStatusEnum.EXPIRED.getCode() != vehicleInsuranceDTO.getCommercialInsuranceStatus()) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "合同约定不包含商业险，请选择其他车辆。");
            }
        }
        TipVO tipVO = new TipVO();
        if (JudgeEnum.NO.getCode().equals(deliverReplaceCmd.getSecondOperationFlag())) {
            // 对车辆的保险状态不做限制
            if (1 == deliverReplaceCmd.getVehicleInsureRequirement()) {
                if (PolicyStatusEnum.EXPIRED.getCode() == vehicleInsuranceDTO.getCompulsoryInsuranceStatus() || PolicyStatusEnum.EXPIRED.getCode() == vehicleInsuranceDTO.getCommercialInsuranceStatus()) {
                    tipVO.setTipFlag(JudgeEnum.YES.getCode());
                    String companyInsureTipMsg = "您选择的车辆里包含保险失效车辆，如果继续，请前往待投保中，对过保车辆发起投保申请。避免因保险未生效影响用户交车。";
                    tipVO.setTipMsg(companyInsureTipMsg);
                    return tipVO;
                }
            } else if (2 == deliverReplaceCmd.getVehicleInsureRequirement()) {
                tipVO.setTipFlag(JudgeEnum.YES.getCode());
                String costumerInsureTipMsg = "选择车辆里包含保险失效车辆，根据合同约定不包含商业险，请联系租户获取保单信息，完成信息回填。没有有效的保单信息无法发车！";
                tipVO.setTipMsg(costumerInsureTipMsg);
                return tipVO;
            }
        }

        DeliverDTO deliverDTO = new DeliverDTO();
        //原车辆未预选状态
        Result<DeliverDTO> deliverDtoResult = deliverAggregateRootApi.getDeliverByServeNo(serveNo);
        DeliverDTO deliver = deliverDtoResult.getData();
        if (deliver == null) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "交付单查询失败");
        }

        VehicleSaveCmd vehicleStatusSaveCmd = new VehicleSaveCmd();
        vehicleStatusSaveCmd.setId(Collections.singletonList(deliver.getCarId()));
        vehicleStatusSaveCmd.setSelectStatus(ValidSelectStatusEnum.UNCHECKED.getCode());
        deliverDTO.setCustomerId(deliver.getCustomerId());
        Result<String> vehicleEditResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleStatusSaveCmd);
        if (vehicleEditResult.getCode() != 0) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), vehicleEditResult.getMsg());
        }

        Result<VehicleInfoDto> vehicleResult = vehicleAggregateRootApi.getVehicleInfoVOById(vehicleId);
        if (vehicleResult.getCode() != 0) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), vehicleResult.getMsg());
        }
        VehicleInfoDto vehicleInfoDto = vehicleResult.getData();
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setId(Collections.singletonList(vehicleId));
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.CHECKED.getCode());
        Result<String> replaceResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if (replaceResult.getCode() != 0) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), replaceResult.getMsg());
        }
        //更换车辆信息 原交付单失效

        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(serveNo);
        ServeDTO serveDTO = ResultDataUtils.getInstance(serveDTOResult).getDataOrException();
        if (null == serveDTO) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "服务单查询失败");
        }
        if ((PolicyStatusEnum.EFFECT.getCode() == vehicleInfoDto.getCompulsoryInsuranceStatus() && PolicyStatusEnum.EFFECT.getCode() == vehicleInfoDto.getCommercialInsuranceStatus()) ||
                PolicyStatusEnum.EFFECT.getCode() == vehicleInfoDto.getCompulsoryInsuranceStatus() && LeaseModelEnum.SHOW.getCode() == serveDTO.getLeaseModelId()) {
            deliverDTO.setIsInsurance(JudgeEnum.YES.getCode());
            Result<VehicleInsuranceDto> vehicleInsuranceDtoResult = vehicleInsuranceAggregateRootApi.getVehicleInsuranceById(vehicleInfoDto.getId());
            if (null != vehicleInsuranceDtoResult.getData()) {
                deliverDTO.setInsuranceStartTime(DeliverUtils.getYYYYMMDDByString(vehicleInsuranceDtoResult.getData().getStartTime()));
            }
        }

        deliverDTO.setServeNo(deliverReplaceCmd.getServeList().get(0));
        deliverDTO.setCarNum(deliverVehicleSelectCmd.getPlateNumber());
        deliverDTO.setCarId(vehicleId);
        deliverDTO.setDeliverStatus(DeliverEnum.IS_DELIVER.getCode());
        deliverDTO.setStatus(ValidStatusEnum.VALID.getCode());
        deliverDTO.setFrameNum(deliverVehicleSelectCmd.getVin());
        deliverDTO.setMileage(deliverVehicleSelectCmd.getMileage());
        deliverDTO.setVehicleAge(deliverVehicleSelectCmd.getVehicleAge());
        deliverDTO.setCarServiceId(deliverReplaceCmd.getCarServiceId());
        deliverDTO.setVehicleBusinessMode(vehicleInfoDto.getVehicleBusinessMode());
        if (PolicyStatusEnum.EXPIRED.getCode() != vehicleInsuranceDTO.getCompulsoryInsuranceStatus()) {
            deliverDTO.setCompulsoryPolicyId(vehicleInsuranceDTO.getCompulsoryInsuranceId().toString());
        }
        if (PolicyStatusEnum.EXPIRED.getCode() != vehicleInsuranceDTO.getCommercialInsuranceStatus()) {
            deliverDTO.setCommercialPolicyId(vehicleInsuranceDTO.getCommercialInsuranceId().toString());
        }
        deliverDTO.setDeliverNo(deliver.getDeliverNo());

        Result<String> result = deliverAggregateRootApi.toReplace(deliverDTO);
        ResultDataUtils.getInstance(result).getDataOrException();

        tipVO.setTipFlag(JudgeEnum.NO.getCode());
        return tipVO;

    }

}
