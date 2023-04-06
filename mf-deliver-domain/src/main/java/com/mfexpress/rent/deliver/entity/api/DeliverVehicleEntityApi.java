package com.mfexpress.rent.deliver.entity.api;

import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.entity.DeliverMethodPO;
import com.mfexpress.rent.deliver.entity.DeliverVehicleEntity;

import java.util.List;

public interface DeliverVehicleEntityApi {

    List<DeliverVehicleDTO> getDeliverVehicleListByDeliverNoList(List<String> deliverNoList);

    DeliverVehicleDTO getDeliverVehicleByDeliverNo(String deliverNo);

    List<DeliverVehicleDTO> getDeliverVehicleByServeNoList(List<String> serveNoList);

    Integer addDeliverVehicle(List<DeliverVehicleEntity> deliverVehicleList);

    Integer saveDeliverMethods(List<DeliverMethodPO> deliverMethodPOS);

}
