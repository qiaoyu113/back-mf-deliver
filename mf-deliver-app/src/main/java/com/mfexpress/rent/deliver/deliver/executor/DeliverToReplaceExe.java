package com.mfexpress.rent.deliver.deliver.executor;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverReplaceCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverVehicleSelectCmd;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInfoDto;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

@Component
public class DeliverToReplaceExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;


    public String toReplace(DeliverReplaceCmd deliverReplaceCmd) {
        DeliverVehicleSelectCmd deliverVehicleSelectCmd = deliverReplaceCmd.getDeliverVehicleSelectCmd();


        serveAggregateRootApi.toReplace(deliverReplaceCmd.getServeNo());

        Result<DeliverDTO> deliverDtoResult = deliverAggregateRootApi.getDeliverByServeNo(deliverReplaceCmd.getServeNo());
        if (deliverDtoResult.getData() != null) {
            DeliverDTO deliverDTO = deliverDtoResult.getData();
            VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
            vehicleSaveCmd.setId(Arrays.asList(deliverDTO.getCarId()));
            vehicleSaveCmd.setSelectStatus(JudgeEnum.NO.getCode());
            vehicleSaveCmd.setStockStatus(1);
            Result<String> vehicleResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
            if (vehicleResult.getCode() != 0) {
                return vehicleResult.getMsg();
            }
        }

        Result<VehicleInfoDto> vehicleResult = vehicleAggregateRootApi.getVehicleInfoVOById(deliverVehicleSelectCmd.getCarId());
        if (vehicleResult.getCode() != 0) {
            return vehicleResult.getMsg();
        }


        //更换车辆信息 原交付单失效
        DeliverDTO deliverDTO = new DeliverDTO();
        deliverDTO.setIsInsurance(vehicleResult.getData().getInsuranceStatus());
        deliverDTO.setServeNo(deliverReplaceCmd.getServeNo());
        deliverDTO.setCarNum(deliverVehicleSelectCmd.getCarNum());
        deliverDTO.setCarId(deliverVehicleSelectCmd.getCarId());
        deliverDTO.setDeliverStatus(DeliverEnum.IS_DELIVER.getCode());
        deliverDTO.setStatus(ValidStatusEnum.VALID.getCode());
        Result<String> result = deliverAggregateRootApi.toReplace(deliverDTO);
        return result.getData();

    }


}
