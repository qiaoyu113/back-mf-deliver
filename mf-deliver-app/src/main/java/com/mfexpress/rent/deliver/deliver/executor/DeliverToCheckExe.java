package com.mfexpress.rent.deliver.deliver.executor;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.api.SyncServiceI;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCheckCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;

@Component
public class DeliverToCheckExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private SyncServiceI syncServiceI;


    public String execute(DeliverCheckCmd deliverCheckCmd) {
        Result<String> result = deliverAggregateRootApi.toCheck(deliverCheckCmd.getServeNo());

        DeliverCarServiceDTO deliverCarServiceDTO = new DeliverCarServiceDTO();
        deliverCarServiceDTO.setServeNoList(Arrays.asList(deliverCheckCmd.getServeNo()));
        deliverCarServiceDTO.setCarServiceId(deliverCheckCmd.getCarServiceId());
        deliverAggregateRootApi.saveCarServiceId(deliverCarServiceDTO);

        // 暂时强同步es
        syncServiceI.execOne(deliverCheckCmd.getServeNo());


        return result.getData();
    }
}


