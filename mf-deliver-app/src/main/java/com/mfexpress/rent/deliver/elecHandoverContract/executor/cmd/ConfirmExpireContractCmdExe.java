package com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.ConfirmExpireContractCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ConfirmExpireContractCmdExe {

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    public Integer execute(ConfirmExpireContractCmd cmd, TokenInfo tokenInfo) {
        // 用户确认后合同过期后，合同所属的交付单后续流程才继续往下走，将交付单置为未签署的状态
        Result<ElecContractDTO> elecContractDTOResult = contractAggregateRootApi.getContractDTOByContractId(cmd.getContractId());
        ElecContractDTO elecContractDTO = ResultDataUtils.getInstance(elecContractDTOResult).getDataOrException();
        String deliverNos = elecContractDTO.getDeliverNos();
        Integer deliverType = elecContractDTO.getDeliverType();

        cmd.setOperatorId(tokenInfo.getId());
        Result<Integer> confirmResult = contractAggregateRootApi.confirmExpireContract(cmd);
        ResultValidUtils.checkResultException(confirmResult);

        Result<Integer> result = deliverAggregateRootApi.makeNoSignByDeliverNo(deliverNos, deliverType);

        return ResultDataUtils.getInstance(result).getDataOrException();
    }
}
