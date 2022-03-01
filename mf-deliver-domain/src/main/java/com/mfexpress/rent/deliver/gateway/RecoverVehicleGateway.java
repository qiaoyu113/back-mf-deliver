package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.dto.entity.RecoverVehicle;

import java.util.List;

public interface RecoverVehicleGateway {


    void addRecoverVehicle(List<RecoverVehicle> recoverVehicleList);

    int updateRecoverVehicle(RecoverVehicle recoverVehicle);

    List<RecoverVehicle> selectRecoverByServeNoList(List<String> serveNoList);

    RecoverVehicle getRecoverVehicleByDeliverNo(String deliverNo);

    List<RecoverVehicle> getRecoverVehicleDtosByDeliverNoList(List<String> deliverNoList);

    List<RecoverVehicle> getRecoverVehicleByDeliverNos(List<String> deliverNoList);


}
