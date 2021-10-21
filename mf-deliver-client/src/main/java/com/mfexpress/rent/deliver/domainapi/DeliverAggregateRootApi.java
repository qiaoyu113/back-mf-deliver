package com.mfexpress.rent.deliver.domainapi;


import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverBackInsureDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverVehicleMqDTO;
import com.mfexpress.rent.deliver.dto.entity.Deliver;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "mf-deliver", path = "/domain/deliver/v3/deliver", contextId = "mf-deliver-aggregate-root-api")
public interface DeliverAggregateRootApi {

    @PostMapping("/getDeliverByServeNo")
    Result<DeliverDTO> getDeliverByServeNo(@RequestParam("serveNo") String serveNo);


    @PostMapping("/addDeliver")
    Result<String> addDeliver(@RequestBody List<DeliverDTO> list);

    @PostMapping("/toCheck")
    Result<Integer> toCheck(@RequestParam("serveNo") String serveNo);

    @PostMapping("/toReplace")
    Result<String> toReplace(@RequestBody DeliverDTO deliverDTO);

    @PostMapping("/toInsure")
    Result<String> toInsure(@RequestBody List<String> serveNoList);

    @PostMapping("/toDeliver")
    Result<String> toDeliver(@RequestBody List<String> serveNoList);

    @PostMapping("/applyRecover")
    Result<String> applyRecover(@RequestBody List<String> serveNoList);

    @PostMapping("/cancelRecover")
    Result<String> cancelRecover(@RequestParam("serveNo") String serveNo);

    @PostMapping("/toBackInsure")
    Result<String> toBackInsure(@RequestBody DeliverBackInsureDTO deliverBackInsureDTO);

    @PostMapping("/toDeduction")
    Result<String> toDeduction(@RequestBody DeliverDTO deliverDTO);

    @PostMapping("/cancelSelected")
    Result<String> cancelSelected(@RequestParam("carId") Integer carId);

    @PostMapping("/cancelSelectedByServeNoList")
    Result<List<Integer>> cancelSelectedByServeNoList(@RequestBody List<String> serveNoList);


    @PostMapping("/syncInsureStatus")
    Result<String> syncInsureStatus(@RequestBody List<DeliverVehicleMqDTO> deliverVehicleMqDTOList);

    @PostMapping("/syncVehicleAgeAndMileage")
    Result<String> syncVehicleAgeAndMileage(@RequestBody List<DeliverVehicleMqDTO> deliverVehicleMqDTOList);

    @PostMapping("/saveCarServiceId")
    Result<String> saveCarServiceId(@RequestBody DeliverCarServiceDTO deliverCarServiceDTO);

    @PostMapping("/getDeliverByServeNoList")
    Result<Map<String, Deliver>> getDeliverByServeNoList(@RequestBody List<String> serveNoList);

    @PostMapping("/getDeduct")
    Result<List<DeliverDTO>> getDeduct(@RequestBody List<String> serveNoList);


}
