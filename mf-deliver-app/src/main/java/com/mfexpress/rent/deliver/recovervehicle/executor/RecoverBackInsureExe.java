package com.mfexpress.rent.deliver.recovervehicle.executor;


import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.DateUtils;
import com.mfexpress.rent.deliver.api.SyncServiceI;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverBackInsureDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverBackInsureCmd;
import com.mfexpress.rent.vehicle.api.VehicleInsuranceAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidInsuranceStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicleinsurance.VehicleInsuranceSaveListCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RecoverBackInsureExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private VehicleInsuranceAggregateRootApi vehicleInsuranceAggregateRootApi;
    @Resource
    private SyncServiceI syncServiceI;


    public String execute(RecoverBackInsureCmd recoverBackInsureCmd) {
        // 交付单 更新已收车 保险状态
        DeliverBackInsureDTO deliverBackInsureDTO = new DeliverBackInsureDTO();
        deliverBackInsureDTO.setServeNoList(recoverBackInsureCmd.getServeNoList());
        deliverBackInsureDTO.setInsuranceRemark(recoverBackInsureCmd.getInsuranceRemark());

        Result<String> serveNoResult = deliverAggregateRootApi.toBackInsure(deliverBackInsureDTO);

        if (recoverBackInsureCmd.getIsInsurance().equals(JudgeEnum.YES.getCode())) {
            // 调用车辆退保 更新车辆退保状态 退保时间 更新车辆未预选状态 库存地
            VehicleInsuranceSaveListCmd vehicleInsuranceSaveListCmd = new VehicleInsuranceSaveListCmd();
            vehicleInsuranceSaveListCmd.setId(recoverBackInsureCmd.getCarIdList());
            vehicleInsuranceSaveListCmd.setInsuranceStatus(ValidInsuranceStatusEnum.INVALID.getCode());
            vehicleInsuranceSaveListCmd.setEndTime(DateUtils.format(recoverBackInsureCmd.getInsuranceTime(), "yyyy-MM-dd"));
            vehicleInsuranceAggregateRootApi.saveVehicleInsuranceById(vehicleInsuranceSaveListCmd);
        }
        DeliverCarServiceDTO deliverCarServiceDTO = new DeliverCarServiceDTO();
        deliverCarServiceDTO.setServeNoList(recoverBackInsureCmd.getServeNoList());
        deliverCarServiceDTO.setCarServiceId(recoverBackInsureCmd.getCarServiceId());
        deliverAggregateRootApi.saveCarServiceId(deliverCarServiceDTO);
        for (String serveNo : recoverBackInsureCmd.getServeNoList()) {
            syncServiceI.execOne(serveNo);
        }
        return serveNoResult.getData();

    }
}
