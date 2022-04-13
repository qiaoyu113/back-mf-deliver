package com.mfexpress.rent.deliver.serve.executor;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ReactivateServeCmdExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    public Integer execute(ReactivateServeCmd cmd, TokenInfo tokenInfo) {
        cmd.setOperatorId(tokenInfo.getId());
        cmd.setOperatorName(tokenInfo.getNickName());
        Result<Integer> operateResult = serveAggregateRootApi.reactiveServe(cmd);
        ResultValidUtils.checkResultException(operateResult);
        return operateResult.getCode();
    }
}
