package com.mfexpress.rent.deliver.api;

import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;

public interface DeliverVehicleServiceI {

    String toDeliver(DeliverVehicleCmd deliverVehicleCmd);
}
