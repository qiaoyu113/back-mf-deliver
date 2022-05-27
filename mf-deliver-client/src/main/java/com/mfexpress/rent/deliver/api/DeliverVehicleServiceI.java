package com.mfexpress.rent.deliver.api;

import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleVO;
import com.mfexpress.transportation.customer.dto.entity.vo.LinkmanVo;

public interface DeliverVehicleServiceI {

    String toDeliver(DeliverVehicleCmd deliverVehicleCmd);

    LinkmanVo getDeliverByDeliverNo(Integer customerCmd);
}
