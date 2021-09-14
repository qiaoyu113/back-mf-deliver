package com.mfexpress.rent.deliver.deliver.executor;


import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverPreselectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverVehicleSelectCmd;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInfoDto;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Component
public class DeliverToPreselectedExe {
    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;


    public String toPreselected(DeliverPreselectedCmd deliverPreselectedCmd) {
        List<DeliverDTO> deliverList = new LinkedList<>();
        //服务单编号
        List<String> serveNoList = deliverPreselectedCmd.getServeList();
        //车辆信息
        List<DeliverVehicleSelectCmd> deliverVehicleSelectCmdList = deliverPreselectedCmd.getDeliverVehicleSelectCmdList();
        //车辆id list
        List<Integer> carIdList = new LinkedList<>();
        for (int i = 0; i < serveNoList.size(); i++) {

            DeliverDTO deliverDTO = new DeliverDTO();
            DeliverVehicleSelectCmd deliverVehicleSelectCmd;
            try {
                deliverVehicleSelectCmd = deliverVehicleSelectCmdList.get(i);
            } catch (IndexOutOfBoundsException e) {
                throw new RuntimeException("批量预选数量错误");
            }


            Result<VehicleInfoDto> vehicleResult = vehicleAggregateRootApi.getVehicleInfoVOById(deliverVehicleSelectCmd.getCarId());
            if (vehicleResult.getCode() != 0 || vehicleResult.getData() == null) {
                return vehicleResult.getMsg();
            }
            deliverDTO.setIsInsurance(vehicleResult.getData().getInsuranceStatus());
            deliverDTO.setServeNo(serveNoList.get(i));
            deliverDTO.setCarId(deliverVehicleSelectCmd.getCarId());
            deliverDTO.setCarNum(deliverVehicleSelectCmd.getCarNum());
            deliverDTO.setDeliverStatus(DeliverEnum.IS_DELIVER.getCode());
            deliverDTO.setStatus(ValidStatusEnum.VALID.getCode());
            deliverDTO.setFrameNum(deliverVehicleSelectCmd.getFrameNum());
            deliverDTO.setMileage(deliverVehicleSelectCmd.getMileage());
            deliverDTO.setVehicleAge(deliverVehicleSelectCmd.getVehicleAge());
            deliverList.add(deliverDTO);
            carIdList.add(deliverVehicleSelectCmd.getCarId());

        }

        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setId(carIdList);
        vehicleSaveCmd.setStockStatus(1);
        vehicleSaveCmd.setSelectStatus(JudgeEnum.YES.getCode());
        Result<String> vehicleResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if (vehicleResult.getCode() != 0) {
            return vehicleResult.getMsg();
        }

        Result<String> serveResult = serveAggregateRootApi.toPreselected(serveNoList);
        if (serveResult.getCode() != 0) {
            return serveResult.getMsg();
        }
        Result<String> deliverResult = deliverAggregateRootApi.addDeliver(deliverList);
        if (deliverResult.getCode() != 0) {
            return deliverResult.getMsg();
        }

        return deliverResult.getData();
    }

}
