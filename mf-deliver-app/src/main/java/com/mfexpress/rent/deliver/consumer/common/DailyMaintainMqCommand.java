package com.mfexpress.rent.deliver.consumer.common;

import com.alibaba.fastjson.JSON;
import com.mfexpress.component.starter.mq.relation.common.MFMqCommonProcessClass;
import com.mfexpress.component.starter.mq.relation.common.MFMqCommonProcessMethod;
import com.mfexpress.rent.deliver.domainapi.DailyAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.daily.DailyMaintainDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@MFMqCommonProcessClass(topicKey = "rocketmq.listenEventTopic")
@Slf4j
public class DailyMaintainMqCommand {

    @Resource
    private DailyAggregateRootApi dailyAggregateRootApi;


    @MFMqCommonProcessMethod(tag = "maintain_daily")
    public void execute(String body) {
        log.info("发起维修通知日报处理，{}", body);
        DailyMaintainDTO dailyMaintainDTO = JSON.parseObject(body, DailyMaintainDTO.class);
        dailyAggregateRootApi.maintainDaily(dailyMaintainDTO);
    }
}
