package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverInvalidCmd;
import com.mfexpress.rent.deliver.entity.RecoverVehicleEntity;

import java.util.List;

public interface RecoverVehicleGateway {


    void addRecoverVehicle(List<RecoverVehicleEntity> recoverVehicleList);

    int updateRecoverVehicle(RecoverVehicleEntity recoverVehicle);

    List<RecoverVehicleEntity> selectRecoverByServeNoList(List<String> serveNoList);

    RecoverVehicleEntity getRecoverVehicleByDeliverNo(String deliverNo);

    List<RecoverVehicleEntity> getRecoverVehicleByDeliverNoList(List<String> deliverNoList);

    List<RecoverVehicleEntity> getRecoverVehicleByDeliverNos(List<String> deliverNoList);

    int updateRecoverVehicleByDeliverNo(RecoverVehicleEntity recoverVehicle);

    int invalidRecover(RecoverInvalidCmd cmd);

    List<RecoverVehicleEntity> getRecoverVehicleByServeNos(List<String> serveNos);
}
