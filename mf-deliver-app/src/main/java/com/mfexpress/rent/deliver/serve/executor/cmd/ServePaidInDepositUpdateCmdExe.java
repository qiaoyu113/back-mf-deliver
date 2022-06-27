package com.mfexpress.rent.deliver.serve.executor.cmd;

import javax.annotation.Resource;

import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServePaidInDepositUpdateCmd;
import org.springframework.stereotype.Component;

@Component
public class ServePaidInDepositUpdateCmdExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    public void excute(ServePaidInDepositUpdateCmd cmd) {

        serveAggregateRootApi.updateServePaidInDeposit(cmd);
    }
}
