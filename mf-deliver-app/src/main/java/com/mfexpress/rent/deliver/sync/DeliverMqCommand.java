package com.mfexpress.rent.deliver.sync;

import com.alibaba.fastjson.JSON;
import com.mfexpress.component.dto.mq.BaseCommand;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.RedisTools;
import com.mfexpress.rent.deliver.api.SyncServiceI;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAddDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DeliverMqCommand extends BaseCommand {
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private RedisTools redisTools;
    @Resource
    private SyncServiceI syncServiceI;

    @Override
    public void execute(String body) {
        long start = System.currentTimeMillis();
        log.info(body);
        ServeAddDTO serveAddDTO = JSON.parseObject(body, ServeAddDTO.class);
        //暂时使用redis 增加幂等性校验
        Object o = redisTools.get(serveAddDTO.getOrderId().toString());
        if (o == null) {
            Result<String> result = serveAggregateRootApi.addServe(serveAddDTO);
            long end = System.currentTimeMillis();
            System.out.println("====================================" + start);
            System.out.println("====================================" + end);
            System.out.println(end - start);
        }

    }
}
