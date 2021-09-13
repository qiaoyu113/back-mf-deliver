package com.mfexpress.rent.deliver.domainapi;


import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverBackInsureDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mf-deliver", path = "/domain/deliver/v3/deliver", contextId = "mf-deliver-aggregate-root-api")
public interface DeliverAggregateRootApi {

    @PostMapping("/getDeliverByServeNo")
    Result<DeliverDTO> getDeliverByServeNo(@RequestParam("serveNo") String serveNo);


    @PostMapping("/addDeliver")
    Result<String> addDeliver(@RequestBody List<DeliverDTO> list);

    @PostMapping("/toCheck")
    Result<String> toCheck(@RequestParam("serveNo") String serveNo);

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
    Result<String>toDeduction(@RequestBody DeliverDTO deliverDTO);
}
