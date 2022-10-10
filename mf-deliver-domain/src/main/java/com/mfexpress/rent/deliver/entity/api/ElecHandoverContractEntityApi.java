package com.mfexpress.rent.deliver.entity.api;

import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.*;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ContractIdWithDocIds;

public interface ElecHandoverContractEntityApi {
    
    ContractIdWithDocIds createDeliverContract(CreateDeliverContractCmd cmd);

    ContractIdWithDocIds createRecoverContract(CreateRecoverContractCmd cmd);

    Integer cancelContract(CancelContractCmd cmd);

    Integer completionContractForeignNo(ContractStatusChangeCmd cmd);

    Integer signing(ContractStatusChangeCmd cmd);

    Integer completed(ContractStatusChangeCmd cmd);

    Integer fail(ContractStatusChangeCmd cmd);

    Integer confirmFailContract(ConfirmFailCmd cmd);

    Integer incrSendSmsCount(Long contractId);

    Integer autoCompleted(ContractStatusChangeCmd cmd);

}
