package com.mfexpress.rent.deliver.domainapi;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(name = "mf-deliver", path = "/domain/deliver/v3/delivervehcile", contextId = "mf-deliver-vehicle-aggregate-root-api")
public interface DeliverVehicleAggregateRootApi {

    @PostMapping("/getDeliverVehicleDto")
    Result<DeliverVehicleDTO> getDeliverVehicleDto(@RequestParam("deliverNo") String deliverNo);

    @PostMapping("/addDeliverVehicle")
    Result<String> addDeliverVehicle(@RequestBody List<DeliverVehicleDTO> deliverVehicleDTOList);
}
