package com.mfexpress.rent.deliver.deliver.executor;

import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCheckCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCheckCmd;
import com.mfexpress.rent.deliver.serve.executor.ReactiveServeCheckCmdExe;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class DeliverToCheckExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource(name = "serveSyncServiceImpl")
    private EsSyncHandlerI syncServiceI;

    @Resource
    private ReactiveServeCheckCmdExe reactiveServeCheck;

    public String execute(DeliverCheckCmd deliverCheckCmd) {
        // 重新激活的服务单在进行验车操作时需要的校验
        ReactivateServeCheckCmd reactivateServeCheckCmd = ReactivateServeCheckCmd.builder().serveNoList(Collections.singletonList(deliverCheckCmd.getServeNo())).build();
        reactiveServeCheck.execute(reactivateServeCheckCmd);

        Result<Integer> result = deliverAggregateRootApi.toCheck(deliverCheckCmd.getServeNo(), deliverCheckCmd.getCarServiceId());
        ResultValidUtils.checkResultException(result);

        // 同样是对deliver的操作确分为了两步，下面的保存操作人的操作整合到了上面的验车接口中
        /*DeliverCarServiceDTO deliverCarServiceDTO = new DeliverCarServiceDTO();
        deliverCarServiceDTO.setServeNoList(Arrays.asList(deliverCheckCmd.getServeNo()));
        deliverCarServiceDTO.setCarServiceId(deliverCheckCmd.getCarServiceId());
        deliverAggregateRootApi.saveCarServiceId(deliverCarServiceDTO);*/

        // 暂时强同步es
        HashMap<String, String> map = new HashMap<>();
        map.put("serve_no", deliverCheckCmd.getServeNo());
        syncServiceI.execOne(map);

        return result.getMsg();
    }

}


