package com.mfexpress.rent.deliver.deliver.executor;

import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.DateUtils;
import com.mfexpress.rent.deliver.api.SyncServiceI;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverInsureCmd;
import com.mfexpress.rent.vehicle.api.VehicleInsuranceAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.vehicleinsurance.VehicleInsuranceSaveListCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Component
public class DeliverToInsureExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private VehicleInsuranceAggregateRootApi vehicleInsuranceAggregateRootApi;
    @Resource
    private SyncServiceI syncServiceI;


    public String execute(DeliverInsureCmd deliverInsureCmd) {
        List<String> serveNoList = deliverInsureCmd.getServeNoList();
        //更新投保状态
        //调用车辆 更新投保时间段
        VehicleInsuranceSaveListCmd vehicleInsuranceSaveListCmd = new VehicleInsuranceSaveListCmd();
        vehicleInsuranceSaveListCmd.setStartTime(DateUtils.format(deliverInsureCmd.getStartInsureDate(), "yyyy-MM-dd"));
        vehicleInsuranceSaveListCmd.setEndTime(DateUtils.format(deliverInsureCmd.getEndInsureDate(), "yyyy-MM-dd"));
        vehicleInsuranceSaveListCmd.setInsuranceStatus(ValidStatusEnum.VALID.getCode());
        vehicleInsuranceSaveListCmd.setId(deliverInsureCmd.getCarIdList());
        Result<String> vehicleResult = vehicleInsuranceAggregateRootApi.saveVehicleInsuranceById(vehicleInsuranceSaveListCmd);
        if (vehicleResult.getCode() != 0) {
            return vehicleResult.getMsg();
        }

        Date insuranceStartTime = deliverInsureCmd.getStartInsureDate();
        Result<String> deliverResult = deliverAggregateRootApi.toInsure(deliverInsureCmd);
        DeliverCarServiceDTO deliverCarServiceDTO = new DeliverCarServiceDTO();
        deliverCarServiceDTO.setServeNoList(serveNoList);
        deliverCarServiceDTO.setCarServiceId(deliverInsureCmd.getCarServiceId());
        deliverAggregateRootApi.saveCarServiceId(deliverCarServiceDTO);

        for (String serveNo : serveNoList) {
            syncServiceI.execOne(serveNo);
        }

        return deliverResult.getData();

    }
}
