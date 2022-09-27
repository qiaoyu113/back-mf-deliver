package com.mfexpress.rent.deliver.deliver.executor;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.CancelPreSelectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.DeliverReplaceVehicleCheckCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.TipVO;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;

@Component
public class CancelPreSelectedCmdExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private ReplaceVehicleCheckCmdExe replaceVehicleCheckCmdExe;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    public TipVO execute(CancelPreSelectedCmd cmd, TokenInfo tokenInfo) {
        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getLastDeliverByCarId(cmd.getVehicleId());
        DeliverDTO deliverDTO = ResultDataUtils.getInstance(deliverDTOResult).getDataOrException();
        if (null == deliverDTO) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "交付单查询失败");
        }
        if (!DeliverEnum.IS_DELIVER.getCode().equals(deliverDTO.getDeliverStatus())){
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "交付单状态异常");
        }

        if (JudgeEnum.NO.getCode().equals(cmd.getSecondOperationFlag())) {
            // 返回提示信息
            DeliverReplaceVehicleCheckCmd deliverReplaceVehicleCheckCmd = new DeliverReplaceVehicleCheckCmd();
            deliverReplaceVehicleCheckCmd.setDeliverNo(deliverDTO.getDeliverNo());
            TipVO tipVO = replaceVehicleCheckCmdExe.execute(deliverReplaceVehicleCheckCmd);
            if (JudgeEnum.YES.getCode().equals(tipVO.getTipFlag())) {
                return tipVO;
            }
        }

        // 执行取消预选操作
        cmd.setOperatorId(tokenInfo.getId());
        cmd.setServeNo(deliverDTO.getServeNo());
        cmd.setDeliverNo(deliverDTO.getDeliverNo());
        Result<Integer> editDeliverResult = deliverAggregateRootApi.cancelSelectedByDeliver(cmd);
        ResultDataUtils.getInstance(editDeliverResult).getDataOrException();

        // 修改车辆状态
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setId(Collections.singletonList(cmd.getVehicleId()));
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.UNCHECKED.getCode());
        vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);

        TipVO tipVO = new TipVO();
        tipVO.setTipFlag(JudgeEnum.NO.getCode());
        return tipVO;
    }

}
