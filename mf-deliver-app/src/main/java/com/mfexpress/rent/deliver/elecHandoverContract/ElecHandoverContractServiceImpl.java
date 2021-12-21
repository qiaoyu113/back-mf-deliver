package com.mfexpress.rent.deliver.elecHandoverContract;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.api.ElecHandoverContractServiceI;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.*;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractListQry;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractQry;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.*;
import com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd.*;
import com.mfexpress.rent.deliver.elecHandoverContract.executor.qry.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ElecHandoverContractServiceImpl implements ElecHandoverContractServiceI {

    @Resource
    private CreateDeliverContractCmdExe createDeliverContractCmdExe;

    @Resource
    private CreateRecoverContractCmdExe createRecoverContractCmdExe;

    @Resource
    private ContractStatusQryExe contractStatusQryExe;

    @Resource
    private ContractListQryExe contractListQryExe;

    @Resource
    private ContractDeliverQryExe contractDeliverQryExe;

    @Resource
    private ContractRecoverQryExe contractRecoverQryExe;

    @Resource
    private SendSmsCmdExe sendSmsCmdExe;

    @Resource
    private CancelContractCmdExe cancelContractCmdExe;

    @Resource
    private ConfirmExpireContractCmdExe confirmExpireContractCmdExe;

    @Resource
    private ContractOperationRecordQryExe operationRecordQryExe;

    @Override
    public Long createDeliverContract(CreateDeliverContractCmd cmd, TokenInfo tokenInfo) {
        return createDeliverContractCmdExe.execute(cmd, tokenInfo);
    }

    @Override
    public Long createRecoverContract(CreateRecoverContractFrontCmd cmd, TokenInfo tokenInfo) {
        return createRecoverContractCmdExe.execute(cmd,tokenInfo);
    }

    @Override
    public DeliverContractListVO getDeliverContractList(ContractListQry qry, TokenInfo tokenInfo) {
        return contractListQryExe.execute(qry, tokenInfo);
    }

    @Override
    public List<ElecContractOperationRecordVO> getContractOperationRecord(ContractQry qry, TokenInfo tokenInfo) {
        return operationRecordQryExe.execute(qry, tokenInfo);
    }

    @Override
    public Integer sendSms(SendSmsCmd cmd, TokenInfo tokenInfo) {
        return sendSmsCmdExe.execute(cmd, tokenInfo);
    }

    @Override
    public Integer cancelContract(CancelContractCmd cmd, TokenInfo tokenInfo) {
        return cancelContractCmdExe.execute(cmd, tokenInfo);
    }

    @Override
    public Integer getContractCreateStatus(ContractQry qry, TokenInfo tokenInfo) {
        return contractStatusQryExe.execute(qry, tokenInfo);
    }

    @Override
    public Integer confirmExpireContract(ConfirmExpireContractCmd cmd, TokenInfo tokenInfo) {
        return confirmExpireContractCmdExe.execute(cmd, tokenInfo);
    }

    @Override
    public ElecDeliverContractVO getDeliverContractInfo(ContractQry qry, TokenInfo tokenInfo) {
        return contractDeliverQryExe.execute(qry, tokenInfo);
    }

    @Override
    public ElecRecoverContractVO getRecoverContractInfo(ContractQry qry, TokenInfo tokenInfo) {
        return contractRecoverQryExe.execute(qry, tokenInfo);
    }
}
