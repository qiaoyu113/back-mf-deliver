package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverCancelByDeliverCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RecoverCancelByDeliverExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;


    public Integer execute(RecoverCancelByDeliverCmd cmd, TokenInfo tokenInfo) {

        //交付单回退到已发车状态
        cmd.setOperatorId(tokenInfo.getId());
        cmd.setOperatorName(tokenInfo.getNickName());
        Result<Integer> deliverResult = deliverAggregateRootApi.cancelRecoverByDeliver(cmd);
        if (deliverResult.getCode() != 0) {
            throw new CommonException(deliverResult.getCode(), deliverResult.getMsg());
        }

        return 0;
    }
}
