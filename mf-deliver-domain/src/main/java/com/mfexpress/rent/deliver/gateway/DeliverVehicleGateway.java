package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.entity.DeliverVehicleEntity;

import java.util.List;

public interface DeliverVehicleGateway {

    int addDeliverVehicle(List<DeliverVehicleEntity> deliverVehicleList);

    DeliverVehicleEntity getDeliverVehicleByDeliverNo(String deliverNo);
    DeliverVehicleEntity getDeliverVehicleOneByServeNo(String serveNo);
    List<DeliverVehicleEntity>getDeliverVehicleByServeNo(List<String>serveNoList);
    List<DeliverVehicleEntity>getDeliverVehicleByDeliverNoList(List<String>deliverNoList);

    List<DeliverVehicleEntity> getDeliverVehicleByServeNoList(List<String> serveNoList);


}
