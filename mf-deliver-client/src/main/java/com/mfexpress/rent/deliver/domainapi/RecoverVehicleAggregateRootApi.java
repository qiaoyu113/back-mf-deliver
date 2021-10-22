package com.mfexpress.rent.deliver.domainapi;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.entity.RecoverVehicle;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "mf-deliver", path = "/domain/deliver/v3/recovervehicle", contextId = "mf-deliver-recover-aggregate-root-api")
public interface RecoverVehicleAggregateRootApi {

    @PostMapping("/getRecoverVehicleDtoByDeliverNo")
    Result<RecoverVehicleDTO> getRecoverVehicleDtoByDeliverNo(@RequestParam("deliverNo") String deliverNo);

    @PostMapping("/addRecoverVehicle")
    Result<String> addRecoverVehicle(@RequestBody List<RecoverVehicleDTO> recoverVehicleDTOList);

    @PostMapping("/cancelRecover")
    Result<String> cancelRecover(@RequestBody RecoverVehicleDTO recoverVehicleDTO);

    @PostMapping("/toCheck")
    Result<String> toCheck(@RequestBody RecoverVehicleDTO recoverVehicleDTO);

    @PostMapping("/toBackInsure")
    Result<List<RecoverVehicleDTO>> toBackInsure(@RequestBody List<String> serveNo);

    @PostMapping("/getRecoverVehicleByServeNo")
    Result<Map<String, RecoverVehicle>> getRecoverVehicleByServeNo(@RequestBody List<String> serveNoList);


}
