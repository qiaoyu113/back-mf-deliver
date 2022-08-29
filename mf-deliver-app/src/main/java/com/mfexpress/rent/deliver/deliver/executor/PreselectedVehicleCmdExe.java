package com.mfexpress.rent.deliver.deliver.executor;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.LeaseModelEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverPreselectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverVehicleSelectCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.TipVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.PolicyStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInsuranceDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PreselectedVehicleCmdExe {

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private DeliverToPreselectedExe deliverToPreselectedExe;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    public TipVO execute(DeliverPreselectedCmd cmd, TokenInfo tokenInfo) {
        List<DeliverVehicleSelectCmd> deliverVehicleSelectCmdList = cmd.getDeliverVehicleSelectCmdList();
        List<Integer> vehicleIds = deliverVehicleSelectCmdList.stream().map(DeliverVehicleSelectCmd::getId).collect(Collectors.toList());
        Result<List<VehicleInsuranceDTO>> vehicleInsuranceDTOSResult = vehicleAggregateRootApi.getVehicleInsuranceByVehicleIds(vehicleIds);
        List<VehicleInsuranceDTO> vehicleInsuranceDTOS = ResultDataUtils.getInstance(vehicleInsuranceDTOSResult).getDataOrException();
        if (null == vehicleInsuranceDTOS || vehicleInsuranceDTOS.isEmpty() || vehicleInsuranceDTOS.size() != vehicleIds.size()) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆保险信息查询失败");
        }

        Map<Integer, VehicleInsuranceDTO> vehicleInsuranceDTOMap = vehicleInsuranceDTOS.stream().collect(Collectors.toMap(VehicleInsuranceDTO::getVehicleId, Function.identity(), (v1, v2) -> v1));
        for (DeliverVehicleSelectCmd deliverVehicleSelectCmd : deliverVehicleSelectCmdList) {
            VehicleInsuranceDTO vehicleInsuranceDTO = vehicleInsuranceDTOMap.get(deliverVehicleSelectCmd.getId());
            if (null == vehicleInsuranceDTO) {
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆".concat(deliverVehicleSelectCmd.getPlateNumber()).concat("保险信息查询失败"));
            }
            if (null == vehicleInsuranceDTO.getCompulsoryInsuranceStatus() || null == vehicleInsuranceDTO.getCommercialInsuranceStatus()) {
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆".concat(deliverVehicleSelectCmd.getPlateNumber()).concat("的交强险或商业险状态异常"));
            }
            if (PolicyStatusEnum.EXPIRED.getCode() != vehicleInsuranceDTO.getCompulsoryInsuranceStatus()) {
                if (StringUtils.isEmpty(vehicleInsuranceDTO.getCompulsoryInsuranceId())) {
                    throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆".concat(deliverVehicleSelectCmd.getPlateNumber()).concat("的交强险在保，但保单缺失"));
                }
                deliverVehicleSelectCmd.setCompulsoryPolicyId(vehicleInsuranceDTO.getCompulsoryInsuranceId().toString());
            }
            if (PolicyStatusEnum.EXPIRED.getCode() != vehicleInsuranceDTO.getCommercialInsuranceStatus()) {
                if (StringUtils.isEmpty(vehicleInsuranceDTO.getCommercialInsuranceId())) {
                    throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆".concat(deliverVehicleSelectCmd.getPlateNumber()).concat("的商业险在保，但保单缺失"));
                }
                deliverVehicleSelectCmd.setCommercialPolicyId(vehicleInsuranceDTO.getCommercialInsuranceId().toString());
            }
        }

        if (2 == cmd.getVehicleInsureRequirement()) {
            // 只能选择交强险在保而商业险不在保的车辆
            for (VehicleInsuranceDTO vehicleInsuranceDTO : vehicleInsuranceDTOS) {
                if (PolicyStatusEnum.EFFECT.getCode() != vehicleInsuranceDTO.getCompulsoryInsuranceStatus() || PolicyStatusEnum.EXPIRED.getCode() != vehicleInsuranceDTO.getCommercialInsuranceStatus()) {
                    throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "所选车辆中包含交强险在保而商业险不在保的车辆，请重新确认！");
                }
            }
        }

        TipVO tipVO = new TipVO();
        if (JudgeEnum.NO.getCode().equals(cmd.getSecondOperationFlag())) {
            // 对车辆的保险状态不做限制
            if (1 == cmd.getVehicleInsureRequirement()) {
                for (VehicleInsuranceDTO vehicleInsuranceDTO : vehicleInsuranceDTOS) {
                    if (PolicyStatusEnum.EXPIRED.getCode() == vehicleInsuranceDTO.getCompulsoryInsuranceStatus() || PolicyStatusEnum.EXPIRED.getCode() == vehicleInsuranceDTO.getCommercialInsuranceStatus()) {
                        tipVO.setTipFlag(JudgeEnum.YES.getCode());
                        String companyInsureTipMsg = "您选择的车辆里包含保险失效车辆，如果继续，请前往待投保中，对过保车辆发起投保申请。避免因保险未生效影响用户交车。";
                        tipVO.setTipMsg(companyInsureTipMsg);
                        return tipVO;
                    }
                }
            } else if (2 == cmd.getVehicleInsureRequirement()) {
                Result<List<ServeDTO>> serveDTOSResult = serveAggregateRootApi.getServeDTOByServeNoList(cmd.getServeList());
                List<ServeDTO> serveDTOS = ResultDataUtils.getInstance(serveDTOSResult).getDataOrException();
                for (ServeDTO serveDTO : serveDTOS) {
                    if (LeaseModelEnum.SHOW.getCode() != serveDTO.getLeaseModelId()) {
                        tipVO.setTipFlag(JudgeEnum.YES.getCode());
                        String costumerInsureTipMsg = "选择车辆里包含保险失效车辆，根据合同约定不包含商业险，请联系租户获取保单信息，完成信息回填。没有有效的保单信息无法发车！";
                        tipVO.setTipMsg(costumerInsureTipMsg);
                        return tipVO;
                    }
                }
            }
        }

        cmd.setCarServiceId(tokenInfo.getId());
        deliverToPreselectedExe.execute(cmd);

        tipVO.setTipFlag(JudgeEnum.NO.getCode());
        return tipVO;
    }
}
