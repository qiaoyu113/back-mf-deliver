package com.mfexpress.rent.deliver.recovervehicle.executor;

import cn.hutool.core.date.DateUtil;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverBackInsureDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverBackInsureByDeliverCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverBackInsureCmd;
import com.mfexpress.rent.vehicle.api.VehicleInsuranceAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidInsuranceStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicleinsurance.VehicleInsuranceSaveListCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class RecoverBackInsureByDeliverExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private VehicleInsuranceAggregateRootApi vehicleInsuranceAggregateRootApi;

    @Resource(name = "serveSyncServiceImpl")
    private EsSyncHandlerI syncServiceI;

    public Integer execute(RecoverBackInsureByDeliverCmd cmd, TokenInfo tokenInfo) {
        // 交付单 更新已收车 保险状态
        cmd.setCarServiceId(tokenInfo.getId());
        Result<Integer> deliverResult = deliverAggregateRootApi.toBackInsureByDeliver(cmd);

        if (deliverResult.getCode() != 0) {
            throw new CommonException(deliverResult.getCode(), deliverResult.getMsg());
        }
        if (cmd.getIsInsurance().equals(JudgeEnum.YES.getCode())) {
            // 调用车辆退保 更新车辆退保状态 退保时间 更新车辆未预选状态 库存地
            VehicleInsuranceSaveListCmd vehicleInsuranceSaveListCmd = new VehicleInsuranceSaveListCmd();
            vehicleInsuranceSaveListCmd.setId(cmd.getCarIdList());
            vehicleInsuranceSaveListCmd.setInsuranceStatus(ValidInsuranceStatusEnum.INVALID.getCode());
            vehicleInsuranceSaveListCmd.setEndTime(DateUtil.formatDate(cmd.getInsuranceTime()));
            vehicleInsuranceSaveListCmd.setStartTime("");
            vehicleInsuranceAggregateRootApi.saveVehicleInsuranceById(vehicleInsuranceSaveListCmd);
        }

        return deliverResult.getData();
    }

}
