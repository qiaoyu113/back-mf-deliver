package com.mfexpress.rent.deliver.scheduler;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.RedisTools;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.PassiveRenewalServeCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Resource
    private RedisTools redisTools;

    @Value("${spring.profiles}")
    private String envVariable;

    private final List<Integer> defaultStatuses = Arrays.asList(ServeEnum.DELIVER.getCode(), ServeEnum.RECOVER.getCode());

    private final int limit = 100;

    // 每天的0时10分0秒执行一次
    @Scheduled(cron = "0 10 0 * * *")
    public void process() {
        // 生产环境同一个服务多机器部署，可能会出现多机器同时执行定时任务的情况，这里用了全局锁，使用redis的set类型，谁设置成功谁执行
        // 过期时间1分钟
        long l = redisTools.sSetAndTime(envVariable + ":mf-deliver:serve_recover_time_out_passive_renewal_lock_key", 60, 0);
        // 设置不成功会返回0
        if(l == 0){
            return;
        }
        log.info("超时未收车的服务单自动续约定时任务开始执行");
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
