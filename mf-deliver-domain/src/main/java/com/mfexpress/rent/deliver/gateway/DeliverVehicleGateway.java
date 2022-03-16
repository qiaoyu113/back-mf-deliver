package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.entity.DeliverVehicleEntity;

import java.util.List;

public interface DeliverVehicleGateway {

    int addDeliverVehicle(List<DeliverVehicleEntity> deliverVehicleList);

    DeliverVehicleEntity getDeliverVehicleByDeliverNo(String deliverNo);
    List<DeliverVehicleEntity>getDeliverVehicleByServeNo(List<String>serveNoList);

    List<DeliverVehicleEntity>getDeliverVehicleByDeliverNoList(List<String>deliverNoList);
}
