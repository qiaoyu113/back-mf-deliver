package com.mfexpress.rent.deliver.recovervehicle.executor;


import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.api.SyncServiceI;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVechicleCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.WarehouseAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidStockStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.rent.vehicle.data.dto.warehouse.WarehouseDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

@Component
public class RecoverToCheckExe {

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private SyncServiceI syncServiceI;
    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;
    @Resource
    private WarehouseAggregateRootApi warehouseAggregateRootApi;
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    public String execute(RecoverVechicleCmd recoverVechicleCmd) {
        //完善收车单信息
        RecoverVehicleDTO recoverVehicleDTO = new RecoverVehicleDTO();
        BeanUtils.copyProperties(recoverVechicleCmd, recoverVehicleDTO);

        Result<String> recoverResult = recoverVehicleAggregateRootApi.toCheck(recoverVehicleDTO);
        if (recoverResult.getCode() != 0) {
            return recoverResult.getMsg();
        }
        //更新交付单状态未 已验车 已收车
        Result<Integer> deliverResult = deliverAggregateRootApi.toCheck(recoverVechicleCmd.getServeNo());
        if (deliverResult.getCode() == 0) {
            //更新车辆状态
            VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
            vehicleSaveCmd.setId(Arrays.asList(deliverResult.getData()));
            vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.UNCHECKED.getCode());
            vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.IN.getCode());
            vehicleSaveCmd.setWarehouseId(recoverVechicleCmd.getWareHouseId());
            Result<WarehouseDto> wareHouseResult = warehouseAggregateRootApi.getWarehouseById(vehicleSaveCmd.getWarehouseId());
            if (wareHouseResult.getData() != null) {
                vehicleSaveCmd.setAddress(wareHouseResult.getData().getName());
            }
            vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        }
        //服务单更新已收车
        Result<String> serveResult = serveAggregateRootApi.recover(Arrays.asList(recoverVechicleCmd.getServeNo()));
        if (serveResult.getCode() != 0) {
            return serveResult.getMsg();
        }
        DeliverCarServiceDTO deliverCarServiceDTO = new DeliverCarServiceDTO();
        deliverCarServiceDTO.setCarServiceId(recoverVechicleCmd.getCarServiceId());
        deliverCarServiceDTO.setServeNoList(Arrays.asList(recoverVechicleCmd.getServeNo()));
        deliverAggregateRootApi.saveCarServiceId(deliverCarServiceDTO);

        //同步
        syncServiceI.execOne(recoverVechicleCmd.getServeNo());
        return deliverResult.getMsg();
    }
}

