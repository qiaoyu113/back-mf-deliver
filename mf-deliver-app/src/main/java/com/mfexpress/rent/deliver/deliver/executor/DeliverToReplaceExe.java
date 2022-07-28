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
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCheckCmd;
import com.mfexpress.rent.deliver.serve.executor.ReactiveServeCheckCmdExe;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.VehicleInsuranceAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInfoDto;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.rent.vehicle.data.dto.vehicleinsurance.VehicleInsuranceDto;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class DeliverToReplaceExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private VehicleInsuranceAggregateRootApi vehicleInsuranceAggregateRootApi;

    @Resource
    private ReactiveServeCheckCmdExe reactiveServeCheck;

    public String execute(DeliverReplaceCmd deliverReplaceCmd) {
        // 重新激活的服务单在进行重新预选操作时需要的校验
        ReactivateServeCheckCmd reactivateServeCheckCmd = ReactivateServeCheckCmd.builder().serveNoList(Collections.singletonList(deliverReplaceCmd.getServeList().get(0))).build();
        reactiveServeCheck.execute(reactivateServeCheckCmd);

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
        VehicleInfoDto vehicleInfoDto = vehicleResult.getData();
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setId(Arrays.asList(deliverVehicleSelectCmd.get(0).getId()));
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.CHECKED.getCode());
        Result<String> replaceResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if (replaceResult.getCode() != 0) {
            return replaceResult.getMsg();
        }
        //更换车辆信息 原交付单失效

        if(JudgeEnum.YES.getCode().equals(vehicleInfoDto.getInsuranceStatus())){
            deliverDTO.setIsInsurance(JudgeEnum.YES.getCode());
            Result<VehicleInsuranceDto> vehicleInsuranceDtoResult = vehicleInsuranceAggregateRootApi.getVehicleInsuranceById(vehicleInfoDto.getId());
            if(null != vehicleInsuranceDtoResult.getData()){
                deliverDTO.setInsuranceStartTime(DeliverUtils.getYYYYMMDDByString(vehicleInsuranceDtoResult.getData().getStartTime()));
            }
        }

        deliverDTO.setServeNo(deliverReplaceCmd.getServeList().get(0));
        deliverDTO.setCarNum(deliverVehicleSelectCmd.get(0).getPlateNumber());
        deliverDTO.setCarId(deliverVehicleSelectCmd.get(0).getId());
        deliverDTO.setDeliverStatus(DeliverEnum.IS_DELIVER.getCode());
        deliverDTO.setStatus(ValidStatusEnum.VALID.getCode());
        deliverDTO.setFrameNum(deliverVehicleSelectCmd.get(0).getVin());
        deliverDTO.setMileage(deliverVehicleSelectCmd.get(0).getMileage());
        deliverDTO.setVehicleAge(deliverVehicleSelectCmd.get(0).getVehicleAge());
        deliverDTO.setCarServiceId(deliverReplaceCmd.getCarServiceId());
        deliverDTO.setVehicleBusinessMode(vehicleInfoDto.getVehicleBusinessMode());
        Result<String> result = deliverAggregateRootApi.toReplace(deliverDTO);

        return result.getData();

    }


}
