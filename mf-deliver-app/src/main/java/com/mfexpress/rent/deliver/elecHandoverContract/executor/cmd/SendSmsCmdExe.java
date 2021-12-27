package com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd;

import com.mfexpress.common.domain.api.ContractAggregateRootApi;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.dto.contract.ContractOperateDTO;
import com.mfexpress.component.enums.contract.ContractModeEnum;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.SendSmsCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SendSmsCmdExe {

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private ContractAggregateRootApi foreignContractAggregateRootApi;

    public Integer execute(SendSmsCmd cmd, TokenInfo tokenInfo) {
        // 不再进行短信是否可发送校验，因为正常情况下，这个接口能被调用说明短信是可以被发送的
        Result<ElecContractDTO> contractDTOResult = contractAggregateRootApi.getContractDTOByContractId(cmd.getContractId());
        ElecContractDTO contractDTO = ResultDataUtils.getInstance(contractDTOResult).getDataOrException();

        ContractOperateDTO contractOperateDTO = new ContractOperateDTO();
        contractOperateDTO.setContractId(Long.valueOf(contractDTO.getContractForeignNo()));
        contractOperateDTO.setContractType(ContractModeEnum.DELIVER.getName());
        Result<Boolean> sendResult = foreignContractAggregateRootApi.sendMsg(contractOperateDTO);
        ResultValidUtils.checkResultException(sendResult);

        // 合同中短信相关的数据需要更新
        Result<Integer> incrCountResult = contractAggregateRootApi.incrSendSmsCount(cmd.getContractId());
        ResultValidUtils.checkResultException(incrCountResult);
        return 0;
    }

}
