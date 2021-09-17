package com.mfexpress.rent.deliver.sync;

import com.alibaba.fastjson.JSON;
import com.mfexpress.component.dto.mq.BaseCommand;
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

    @Override
    public void execute(String body) {
        log.info(body);
        ServeAddDTO serveAddDTO = JSON.parseObject(body, ServeAddDTO.class);
        serveAggregateRootApi.addServe(serveAddDTO);

    }
}
