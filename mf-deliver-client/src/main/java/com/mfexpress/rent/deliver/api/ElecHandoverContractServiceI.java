package com.mfexpress.rent.deliver.api;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.*;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractListQry;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractQry;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.*;

import java.util.List;

public interface ElecHandoverContractServiceI {

    Long createDeliverContract(CreateDeliverContractCmd cmd, TokenInfo tokenInfo);

    Long createRecoverContract(CreateRecoverContractFrontCmd cmd, TokenInfo tokenInfo);

    DeliverContractListVO getDeliverContractList(ContractListQry qry, TokenInfo tokenInfo);

    List<ElecContractOperationRecordVO> getContractOperationRecord(ContractQry qry, TokenInfo tokenInfo);

    Integer sendSms(SendSmsCmd cmd, TokenInfo tokenInfo);

    Integer cancelContract(CancelContractCmd cmd, TokenInfo tokenInfo);

    Integer getContractCreateStatus(ContractQry qry, TokenInfo tokenInfo);

    Integer confirmExpireContract(ConfirmExpireContractCmd cmd, TokenInfo tokenInfo);

    ElecDeliverContractVO getDeliverContractInfo(ContractQry qry, TokenInfo tokenInfo);

    ElecRecoverContractVO getRecoverContractInfo(ContractQry qry, TokenInfo tokenInfo);
}
