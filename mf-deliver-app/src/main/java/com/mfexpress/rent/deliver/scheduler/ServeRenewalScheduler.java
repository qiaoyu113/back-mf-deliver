package com.mfexpress.rent.deliver.scheduler;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.PassiveRenewalServeCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class ServeRenewalScheduler {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    private final List<Integer> defaultStatuses = Arrays.asList(ServeEnum.DELIVER.getCode(), ServeEnum.RECOVER.getCode());

    private final int limit = 100;

    // 每天的0时1分0秒执行一次
    @Scheduled(cron = "0 10 0 * * *")
    public void process() {
        PassiveRenewalServeCmd cmd = new PassiveRenewalServeCmd();
        cmd.setStatuses(defaultStatuses);
        cmd.setLimit(limit);
        Result<Integer> renewalResult = serveAggregateRootApi.passiveRenewalServe(cmd);
        if (ResultErrorEnum.SUCCESSED.getCode() != renewalResult.getCode() || null == renewalResult.getData()) {
            log.error("自动续约失败，msg：" + renewalResult.getMsg());
        }

        log.info("自动续约了" + renewalResult.getData() + "条服务单");
    }
}
