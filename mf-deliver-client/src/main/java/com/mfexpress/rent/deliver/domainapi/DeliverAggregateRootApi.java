package com.mfexpress.rent.deliver.domainapi;


import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.deliver.*;
import com.mfexpress.rent.deliver.dto.entity.Deliver;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
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
    Result<Integer> toCheck(@RequestParam("serveNo") String serveNo, @RequestParam("operatorId") Integer operatorId);

    @PostMapping("/toReplace")
    Result<String> toReplace(@RequestBody DeliverDTO deliverDTO);

    @PostMapping("/toInsure")
    Result<String> toInsure(@RequestBody DeliverInsureCmd cmd);

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

    /* luzheng add 根据车辆id查询其所属服务单id */
    @PostMapping("/getDeliveredDeliverDTOByCarId")
    Result<DeliverDTO> getDeliveredDeliverDTOByCarId(@RequestParam("carId") Integer carId);

    @PostMapping("/contractSigning")
    Result<Integer> contractSigning(@RequestBody @Validated DeliverContractSigningCmd cmd);

    @PostMapping("/makeNoSignByDeliverNo")
    Result<Integer> makeNoSignByDeliverNo(@RequestParam("deliverNos") String deliverNos, @RequestParam("deliverType") Integer deliverType);

    @PostMapping("/getDeliverByDeliverNo")
    Result<DeliverDTO> getDeliverByDeliverNo(@RequestParam("deliverNo") String deliverNo);

    @PostMapping("/contractGenerating")
    Result<Integer> contractGenerating(@RequestBody DeliverContractGeneratingCmd cmd);

    @PostMapping("/getLastDeliverByCarId")
    Result<DeliverDTO> getLastDeliverByCarId(@RequestParam("carId") Integer carId);

    @PostMapping("/getDeliverDTOSByCarIdList")
    Result<List<DeliverDTO>> getDeliverDTOSByCarIdList(@RequestParam("carIds") List<Integer> carIds);


    @PostMapping("/getMakeDeliverDTOSByCarIdList")
    Result<List<DeliverDTO>> getMakeDeliverDTOSByCarIdList(@RequestBody List<Integer> carIds,@RequestParam Integer status);

}
