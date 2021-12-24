package com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.dto.contract.ContractOperateDTO;
import com.mfexpress.component.enums.contract.ContractModeEnum;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.contract.MFContractTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CancelContractCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class CancelContractCmdExe {

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private MFContractTools contractTools;

    public Integer execute(CancelContractCmd cmd, TokenInfo tokenInfo) {
        Result<ElecContractDTO> contractDTOResult = contractAggregateRootApi.getContractDTOByContractId(cmd.getContractId());
        ElecContractDTO contractDTO = ResultDataUtils.getInstance(contractDTOResult).getDataOrException();

        cmd.setOperatorId(tokenInfo.getId());
        Result<Integer> result = contractAggregateRootApi.cancelContract(cmd);
        ResultValidUtils.checkResultException(result);

        Result<Integer> noSignResult = deliverAggregateRootApi.makeNoSignByDeliverNo(contractDTO.getDeliverNos(), contractDTO.getDeliverType());
        ResultValidUtils.checkResultException(noSignResult);

        ContractOperateDTO contractOperateDTO = new ContractOperateDTO();
        contractOperateDTO.setContractId(Long.valueOf(contractDTO.getContractForeignNo()));
        contractOperateDTO.setType(ContractModeEnum.DELIVER.getName());
        contractTools.invalid(contractOperateDTO);
        return 0;
    }

}
