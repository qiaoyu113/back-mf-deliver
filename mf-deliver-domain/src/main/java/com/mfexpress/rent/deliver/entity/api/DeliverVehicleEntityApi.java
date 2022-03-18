package com.mfexpress.rent.deliver.entity.api;

import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;

import java.util.List;

public interface DeliverVehicleEntityApi {

    List<DeliverVehicleDTO> getDeliverVehicleListByDeliverNoList(List<String> deliverNoList);

    DeliverVehicleDTO getDeliverVehicleByDeliverNo(String deliverNo);
}
