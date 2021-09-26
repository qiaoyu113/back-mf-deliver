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
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInfoDto;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Component
public class DeliverToReplaceExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;


    public String execute(DeliverReplaceCmd deliverReplaceCmd) {
        List<DeliverVehicleSelectCmd> deliverVehicleSelectCmd = deliverReplaceCmd.getDeliverVehicleSelectCmd();
        DeliverDTO deliverDTO = new DeliverDTO();
        //原车辆未预选状态
        Result<DeliverDTO> deliverDtoResult = deliverAggregateRootApi.getDeliverByServeNo(deliverReplaceCmd.getServeList().get(0));
        if (deliverDtoResult.getData() != null) {
            DeliverDTO deliver = deliverDtoResult.getData();
            VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
            vehicleSaveCmd.setId(Arrays.asList(deliver.getCarId()));
            vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.UNCHECKED.getCode());
            deliverDTO.setCustomerId(deliver.getCustomerId());
            Result<String> vehicleResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
            if (vehicleResult.getCode() != 0) {
                return vehicleResult.getMsg();
            }
        }

        Result<VehicleInfoDto> vehicleResult = vehicleAggregateRootApi.getVehicleInfoVOById(deliverVehicleSelectCmd.get(0).getId());
        if (vehicleResult.getCode() != 0) {
            return vehicleResult.getMsg();
        }
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setId(Arrays.asList(deliverVehicleSelectCmd.get(0).getId()));
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.CHECKED.getCode());
        Result<String> replaceResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if (replaceResult.getCode() != 0) {
            return replaceResult.getMsg();
        }
        //更换车辆信息 原交付单失效

        deliverDTO.setIsInsurance(vehicleResult.getData().getInsuranceStatus().equals(JudgeEnum.YES.getCode()) ? JudgeEnum.YES.getCode() : JudgeEnum.NO.getCode());
        deliverDTO.setServeNo(deliverReplaceCmd.getServeList().get(0));
        deliverDTO.setCarNum(deliverVehicleSelectCmd.get(0).getPlateNumber());
        deliverDTO.setCarId(deliverVehicleSelectCmd.get(0).getId());
        deliverDTO.setDeliverStatus(DeliverEnum.IS_DELIVER.getCode());
        deliverDTO.setStatus(ValidStatusEnum.VALID.getCode());
        deliverDTO.setFrameNum(deliverVehicleSelectCmd.get(0).getVin());
        deliverDTO.setMileage(deliverVehicleSelectCmd.get(0).getMileage());
        deliverDTO.setVehicleAge(deliverVehicleSelectCmd.get(0).getVehicleAge());
        Result<String> result = deliverAggregateRootApi.toReplace(deliverDTO);

        return result.getData();

    }


}
