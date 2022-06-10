package com.mfexpress.rent.deliver.domainapi;

import java.util.Date;
import java.util.List;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.daily.CreateDailyCmd;
import com.mfexpress.rent.deliver.dto.data.daily.DailyDTO;
import com.mfexpress.rent.deliver.dto.data.daily.DailyMaintainDTO;
import com.mfexpress.rent.deliver.dto.data.daily.DailyOperateCmd;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    /**
     * 收发车操作日志
     *
     * @param cmd
     *
     * @return
     */
    @PostMapping(value = "/deliver/recover/daily")
    Result<Integer> createDaily(@RequestBody CreateDailyCmd cmd);
}
