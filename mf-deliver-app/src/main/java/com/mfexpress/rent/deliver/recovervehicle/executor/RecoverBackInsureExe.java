package com.mfexpress.rent.deliver.recovervehicle.executor;


import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.DateUtils;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverBackInsureDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverBackInsureCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.VehicleInsuranceAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidInsuranceStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidStockStatusEnum;
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


    public String execute(RecoverBackInsureCmd recoverBackInsureCmd) {
        // 交付单 更新已收车 保险状态
        DeliverBackInsureDTO deliverBackInsureDTO = new DeliverBackInsureDTO();
        deliverBackInsureDTO.setServeNoList(recoverBackInsureCmd.getServeNoList());
        deliverBackInsureDTO.setInsuranceRemark(recoverBackInsureCmd.getInsuranceRemark());

        Result<List<String>> serveNoResult = deliverAggregateRootApi.toBackInsure(deliverBackInsureDTO);
        //服务单 更新已收车
        Result<String> serveResult = serveAggregateRootApi.recover(recoverBackInsureCmd.getServeNoList());
        if (serveResult.getCode() != 0) {
            return serveResult.getMsg();
        }

        //存在已经处理违章的服务单 更新服务单为已完成
        if (serveNoResult.getData() != null && !serveNoResult.getData().isEmpty()) {
            serveAggregateRootApi.completedList(serveNoResult.getData());
        }

        Result<List<RecoverVehicleDTO>> recoverResult = recoverVehicleAggregateRootApi.toBackInsure(recoverBackInsureCmd.getServeNoList());
        //车辆库存列表
        List<RecoverVehicleDTO> recoverVehicleDTOList = recoverResult.getData();
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setId(recoverBackInsureCmd.getCarIdList());
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.UNCHECKED.getCode());
        vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.IN.getCode());
        vehicleSaveCmd.setWarehouseId(recoverVehicleDTOList.get(0).getWareHouseId());


        vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
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
        return "";

    }
}
