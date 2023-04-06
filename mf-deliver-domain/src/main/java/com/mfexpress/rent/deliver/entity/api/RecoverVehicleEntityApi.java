package com.mfexpress.rent.deliver.entity.api;

import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.entity.RecoverVehicleEntity;

import java.util.List;

public interface RecoverVehicleEntityApi {

    List<RecoverVehicleDTO> getRecoverListByDeliverNoList(List<String> deliverNoList);

    RecoverVehicleDTO getRecoverVehicleByDeliverNo(String deliverNo);

    List<RecoverVehicleDTO> getRecoverVehicleByServeNos(List<String> serveNos);

    Integer updateRecoverVehicleByDeliverNo(RecoverVehicleEntity recoverVehicleToUpdate);

}
