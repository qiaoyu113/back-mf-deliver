package com.mfexpress.rent.deliver.domainapi;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.daily.DailyDTO;
import com.mfexpress.rent.deliver.dto.data.daily.DailyMaintainDTO;
import com.mfexpress.rent.deliver.dto.data.daily.DailyOperateCmd;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "mf-deliver", path = "/domain/deliver/v3/daily", contextId = "mf-daily-aggregate-root-api")
public interface DailyAggregateRootApi {


    @PostMapping("/createDaily")
    Result createDaily(@RequestBody List<DailyDTO> dailyDTOList);

    @PostMapping("/deliverDaily")
    Result deliverDaily(@RequestBody DailyOperateCmd dailyOperateCmd);

    @PostMapping("/recoverDaily")
    Result recoverDaily(@RequestBody DailyOperateCmd dailyOperateCmd);

    @PostMapping("/maintainDaily")
    Result maintainDaily(@RequestBody DailyMaintainDTO dailyMaintainDTO);

}
