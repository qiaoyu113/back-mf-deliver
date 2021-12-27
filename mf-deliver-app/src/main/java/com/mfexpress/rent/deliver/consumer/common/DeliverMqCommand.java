package com.mfexpress.rent.deliver.consumer.common;

import com.alibaba.fastjson.JSON;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.common.MFMqCommonProcessClass;
import com.mfexpress.component.starter.mq.relation.common.MFMqCommonProcessMethod;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAddDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
@MFMqCommonProcessClass(topicKey = "rocketmq.listenEventTopic")
public class DeliverMqCommand {
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private RedisTools redisTools;
    /*@Resource
    private SyncServiceI syncServiceI;*/

    @MFMqCommonProcessMethod(tag = Constants.DELIVER_ORDER_TAG)
    public void execute(String body) {
        log.info(body);
        ServeAddDTO serveAddDTO = JSON.parseObject(body, ServeAddDTO.class);
        //暂时使用redis 增加幂等性校验
        Object o = redisTools.get(serveAddDTO.getOrderId().toString());
        if (o == null) {
            Result<String> result = serveAggregateRootApi.addServe(serveAddDTO);
        }

    }
}
