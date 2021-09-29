package com.mfexpress.rent.deliver.domainapi;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAddDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServePreselectedDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mf-deliver", path = "/domain/deliver/v3/serve", contextId = "mf-deliver-serve-aggregate-root-api")
public interface ServeAggregateRootApi {

    @PostMapping("/getServeDtoByServeNo")
    Result<ServeDTO> getServeDtoByServeNo(@RequestParam("serveNo") String serveNo);

    @PostMapping("/addServe")
    Result<String> addServe(@RequestBody ServeAddDTO serveAddDTO);


    @PostMapping("/toPreselected")
    Result<String> toPreselected(@RequestBody List<String> serveNoList);

    @PostMapping("/toReplace")
    Result<String> toReplace(@RequestParam("serveNo") String serveNo);

    @PostMapping("/deliver")
    Result<String> deliver(@RequestBody List<String> serveNoList);

    @PostMapping("/recover")
    Result<String> recover(@RequestBody List<String> serveNoList);

    @PostMapping("/completed")
    Result<String> completed(@RequestParam("serveNo") String serveNo);

    @PostMapping("/completedList")
    Result<String> completedList(@RequestBody List<String> serveNoList);

    @PostMapping("/getServePreselectedDTO")
    Result<List<ServePreselectedDTO>> getServePreselectedDTO(@RequestBody List<Long> orderId);

    @PostMapping("/cancelSelected")
    Result<String> cancelSelected(@RequestParam("serveNo") String serveNo);

    @PostMapping("/cancelSelectedList")
    Result<String> cancelSelectedList(@RequestBody List<String> serveNoList);


    @PostMapping("/getServeNoListAll")
    Result<List<String>>getServeNoListAll();



}
