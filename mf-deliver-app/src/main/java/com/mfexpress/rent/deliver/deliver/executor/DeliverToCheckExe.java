package com.mfexpress.rent.deliver.deliver.executor;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCheckCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class DeliverToCheckExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    public String toCheck(DeliverCheckCmd deliverCheckCmd) {
        Result<String> result = deliverAggregateRootApi.toCheck(deliverCheckCmd.getServeNo());
        return result.getData();
    }
}


