package com.mfexpress.rent.deliver.recovervehicle.executor;


import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverBackInsureDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverBackInsureCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.utils.Utils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.VehicleInsuranceAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.rent.vehicle.data.dto.vehicleinsurance.VehicleInsuranceSaveListCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RecoverBackInsureExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;
    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;
    @Resource
    private VehicleInsuranceAggregateRootApi vehicleInsuranceAggregateRootApi;


    public String toBackInsure(RecoverBackInsureCmd recoverBackInsureCmd) {
        // 交付单 更新已收车 保险状态
        DeliverBackInsureDTO deliverBackInsureDTO = new DeliverBackInsureDTO();
        deliverBackInsureDTO.setServeNoList(recoverBackInsureCmd.getServeNoList());
        deliverBackInsureDTO.setInsuranceRemark(recoverBackInsureCmd.getInsuranceRemark());

        deliverAggregateRootApi.toBackInsure(deliverBackInsureDTO);
        //服务单 更新已收车
        serveAggregateRootApi.recover(recoverBackInsureCmd.getServeNoList());

        Result<List<RecoverVehicleDTO>> recoverResult = recoverVehicleAggregateRootApi.toBackInsure(recoverBackInsureCmd.getServeNoList());
        //车辆库存列表
        List<RecoverVehicleDTO> recoverVehicleDTOList = recoverResult.getData();
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setId(recoverBackInsureCmd.getCarIdList());
        vehicleSaveCmd.setSelectStatus(2);
        vehicleSaveCmd.setStockStatus(recoverResult.getData().get(0).getWareHouseId());


        vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if (recoverBackInsureCmd.getIsInsurance().equals(JudgeEnum.YES.getCode())) {
            // 调用车辆退保 更新车辆退保状态 退保时间 更新车辆未预选状态 库存地
            VehicleInsuranceSaveListCmd vehicleInsuranceSaveListCmd = new VehicleInsuranceSaveListCmd();
            vehicleInsuranceSaveListCmd.setId(recoverBackInsureCmd.getCarIdList());
            vehicleInsuranceSaveListCmd.setInsuranceStatus(2);
            vehicleInsuranceSaveListCmd.setEndTime(Utils.getDateByYYMMDD(recoverBackInsureCmd.getInsuranceTime()));
            vehicleInsuranceAggregateRootApi.saveVehicleInsuranceById(vehicleInsuranceSaveListCmd);
        }
        return "";

    }
}
