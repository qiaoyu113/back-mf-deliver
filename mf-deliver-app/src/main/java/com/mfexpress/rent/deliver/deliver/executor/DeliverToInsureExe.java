package com.mfexpress.rent.deliver.deliver.executor;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverInsureCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class DeliverToInsureExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;


    public String toInsure(DeliverInsureCmd deliverInsureCmd) {
        List<String> serveNoList = deliverInsureCmd.getServeNoList();
        //更新投保状态
        Result<String> result = deliverAggregateRootApi.toInsure(serveNoList);
        //todo 调用车辆 更新投保时间段


        return "";

    }
}
