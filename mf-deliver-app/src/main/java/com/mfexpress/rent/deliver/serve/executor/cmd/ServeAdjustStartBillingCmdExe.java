package com.mfexpress.rent.deliver.serve.executor.cmd;

import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustStartBillingCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class ServeAdjustStartBillingCmdExe {

    @Resource
    ServeAggregateRootApi serveAggregateRootApi;

    public void execute(ServeAdjustStartBillingCmd cmd) {

        serveAggregateRootApi.serveAdjustStartBilling(cmd);
    }
}
