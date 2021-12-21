package com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.ContractFailureReasonEnum;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CancelContractCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class CancelContractCmdExe {

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    public Integer execute(CancelContractCmd cmd, TokenInfo tokenInfo) {
        cmd.setFailureReason(ContractFailureReasonEnum.CANCEL.getCode());
        cmd.setOperatorId(tokenInfo.getId());
        Result<Integer> result = contractAggregateRootApi.cancelContract(cmd);
        return ResultDataUtils.getInstance(result).getDataOrException();
    }

}
