package com.mfexpress.rent.deliver.api;

import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleVO;

public interface DeliverVehicleServiceI {

    String toDeliver(DeliverVehicleCmd deliverVehicleCmd);

    DeliverVehicleVO getDeliverByDeliverNo(String deliverNo);
}
