package com.mfexpress.rent.deliver.deliver.executor;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverInsureCmd;
import com.mfexpress.rent.deliver.utils.Utils;
import com.mfexpress.rent.vehicle.api.VehicleInsuranceAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.vehicleinsurance.VehicleInsuranceSaveListCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class DeliverToInsureExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private VehicleInsuranceAggregateRootApi vehicleInsuranceAggregateRootApi;


    public String toInsure(DeliverInsureCmd deliverInsureCmd) {
        List<String> serveNoList = deliverInsureCmd.getServeNoList();
        //更新投保状态
        //调用车辆 更新投保时间段
        VehicleInsuranceSaveListCmd vehicleInsuranceSaveListCmd = new VehicleInsuranceSaveListCmd();
        vehicleInsuranceSaveListCmd.setStartTime(Utils.getDateByYYMMDD(deliverInsureCmd.getStartInsureDate()));
        vehicleInsuranceSaveListCmd.setStartTime(Utils.getDateByYYMMDD(deliverInsureCmd.getEndInsureDate()));
        vehicleInsuranceSaveListCmd.setInsuranceStatus(ValidStatusEnum.VALID.getCode());
        vehicleInsuranceSaveListCmd.setId(deliverInsureCmd.getCarIdList());
        Result<String> vehicleResult = vehicleInsuranceAggregateRootApi.saveVehicleInsuranceById(vehicleInsuranceSaveListCmd);
        if (vehicleResult.getCode() != 0) {
            return vehicleResult.getMsg();
        }

        Result<String> deliverResult = deliverAggregateRootApi.toInsure(serveNoList);


        return deliverResult.getData();

    }
}
