package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.dto.entity.DeliverVehicle;

import java.util.List;

public interface DeliverVehicleGateway {

    int addDeliverVehicle(List<DeliverVehicle> deliverVehicleList);

    DeliverVehicle getDeliverVehicleByDeliverNo(String deliverNo);
}
