package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.po.ElectronicHandoverContractPO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractListQry;

import java.util.List;

public interface ElecHandoverContractGateway {

    int create(ElectronicHandoverContractPO contractPO);

    ElectronicHandoverContractPO getContractByContractId(long contractId);

    int updateContractByContractId(ElectronicHandoverContractPO contractPO);

    int updateContractByContractForeignNo(ElectronicHandoverContractPO contractPO);

    ElectronicHandoverContractPO getContractByContract(ElectronicHandoverContractPO contractPO);

    PagePagination<ElectronicHandoverContractPO> getPageContractDTOSByQry(ContractListQry qry);

    List<ElectronicHandoverContractPO> getContractDTOSByDeliverNosAndDeliverType(List<String> deliverNos, int deliverType);

    ElectronicHandoverContractPO getContractByForeignNo(String foreignNo);

    List<ElectronicHandoverContractPO> getContractDTOSByDeliverNoAndDeliverType(String deliverNo, Integer deliverType);

    List<ElecContractDTO> getContractDTOSByQry(ContractListQry qry);
}
