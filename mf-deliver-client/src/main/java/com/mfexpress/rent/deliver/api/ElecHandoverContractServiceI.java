package com.mfexpress.rent.deliver.api;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.*;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractListQry;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractQry;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.*;

public interface ElecHandoverContractServiceI {

    String createDeliverContract(CreateDeliverContractCmd cmd, TokenInfo tokenInfo);

    String createRecoverContract(CreateRecoverContractFrontCmd cmd, TokenInfo tokenInfo);

    DeliverContractListVO getDeliverContractList(ContractListQry qry, TokenInfo tokenInfo);

    ElecContractOperationRecordWithSmsInfoVO getContractOperationRecord(ContractQry qry, TokenInfo tokenInfo);

    Integer sendSms(SendSmsCmd cmd, TokenInfo tokenInfo);

    Integer cancelContract(CancelContractCmd cmd, TokenInfo tokenInfo);

    Integer getContractCreateStatus(ContractQry qry, TokenInfo tokenInfo);

    Integer confirmFail(ConfirmFailCmd cmd, TokenInfo tokenInfo);

    ElecDeliverContractVO getDeliverContractInfo(ContractQry qry, TokenInfo tokenInfo);

    ElecRecoverContractVO getRecoverContractInfo(ContractQry qry, TokenInfo tokenInfo);
}
