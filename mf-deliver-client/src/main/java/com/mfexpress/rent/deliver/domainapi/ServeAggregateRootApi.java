package com.mfexpress.rent.deliver.domainapi;

import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.ListQry;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/getServePreselectedDTO")
    Result<List<ServePreselectedDTO>> getServePreselectedDTO(@RequestBody List<Long> orderId);

    @PostMapping("/cancelSelected")
    Result<String> cancelSelected(@RequestParam("serveNo") String serveNo);

    @PostMapping("/cancelSelectedList")
    Result<String> cancelSelectedList(@RequestBody List<String> serveNoList);


    @PostMapping("/getServeNoListAll")
    Result<List<String>> getServeNoListAll();

    @PostMapping("/getServeDailyDTO")
    Result<PagePagination<ServeDailyDTO>> getServeDailyDTO(@RequestBody ListQry listQry);

    @PostMapping("/getServeMapByServeNoList")
    Result<Map<String, Serve>> getServeMapByServeNoList(@RequestBody List<String> serveNoList);

    @PostMapping("/getCycleServe")
    Result<PagePagination<ServeDTO>> getCycleServe(@RequestBody ServeCycleQryCmd serveCycleQryCmd);

    @PostMapping("/toRepair")
    Result<String> toRepair(@RequestParam("serveNo") String serveNo);

    @PostMapping("/cancelOrCompleteRepair")
    Result<String> cancelOrCompleteRepair(@RequestParam("serveNo") String serveNo);

    @PostMapping("/addServeForReplaceVehicle")
    Result<String> addServeForReplaceVehicle(@RequestBody ServeReplaceVehicleAddDTO serveAddDTO);

}
