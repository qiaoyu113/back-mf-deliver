package com.mfexpress.rent.deliver.domainapi;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.entity.DeliverVehicle;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


@FeignClient(name = "mf-deliver", path = "/domain/deliver/v3/delivervehcile", contextId = "mf-deliver-vehicle-aggregate-root-api")
public interface DeliverVehicleAggregateRootApi {

    @PostMapping("/getDeliverVehicleDto")
    Result<DeliverVehicleDTO> getDeliverVehicleDto(@RequestParam("deliverNo") String deliverNo);

    @PostMapping("/addDeliverVehicle")
    Result<String> addDeliverVehicle(@RequestBody List<DeliverVehicleDTO> deliverVehicleDTOList);
    /**
     * @deprecated 废弃方法 只能用交付单编号查询发车单
     */
    @PostMapping("/getDeliverVehicleByServeNo")
    Result<Map<String, DeliverVehicle>> getDeliverVehicleByServeNo(@RequestBody List<String> serveNoList);

    @PostMapping("/getDeliverVehicleByServeNo")
    Result<DeliverVehicleDTO> getDeliverVehicleOneByServeNo(@RequestBody String serveNo);

    @PostMapping("/deliverVehicles")
    Result<Integer> deliverVehicles(@RequestBody ElecContractDTO contractDTO);

    @PostMapping("/getDeliverVehicleByDeliverNoList")
    Result<List<DeliverVehicleDTO>> getDeliverVehicleByDeliverNoList(@RequestBody List<String> deliverNoList);


}
