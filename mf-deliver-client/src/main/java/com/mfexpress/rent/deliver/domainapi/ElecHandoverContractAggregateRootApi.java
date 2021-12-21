package com.mfexpress.rent.deliver.domainapi;

import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.*;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ContractIdWithDocIds;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractListQry;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractQry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mf-deliver", path = "/domain/deliver/v3/elecHandoverContract")
public interface ElecHandoverContractAggregateRootApi {

    @PostMapping("/createDeliverContract")
    Result<ContractIdWithDocIds> createDeliverContract(@RequestBody @Validated CreateDeliverContractCmd cmd);

    @PostMapping("/createRecoverContract")
    Result<ContractIdWithDocIds> createRecoverContract(@RequestBody @Validated CreateRecoverContractCmd cmd);

    @PostMapping("/cancelContract")
    Result<Integer> cancelContract(@RequestBody @Validated CancelContractCmd cmd);

    @PostMapping("/completionContractForeignNo")
    Result<Integer> completionContractForeignNo(@RequestBody @Validated ContractStatusChangeCmd cmd);

    @PostMapping("/signing")
    Result<Integer> signing(@RequestBody @Validated ContractStatusChangeCmd cmd);

    @PostMapping("/completed")
    Result<Integer> completed(@RequestBody @Validated ContractStatusChangeCmd cmd);

    @PostMapping("/fail")
    Result<Integer> fail(@RequestBody @Validated ContractStatusChangeCmd cmd);

    @PostMapping("/getContractDTOByContractId")
    Result<ElecContractDTO> getContractDTOByContractId(@RequestParam("contractId") Long contractId);

    @PostMapping("/getPageContractDTOSByQry")
    PagePagination<ElecContractDTO> getPageContractDTOSByQry(@RequestBody @Validated ContractListQry qry);

    @PostMapping("/confirmExpireContract")
    Result<Integer> confirmExpireContract(@RequestBody @Validated ConfirmExpireContractCmd cmd);

    @PostMapping("/getContractDTOSByDeliverNosAndDeliverType")
    Result<List<ElecContractDTO>> getContractDTOSByDeliverNosAndDeliverType(@RequestParam("deliverNos") List<String> deliverNos, @RequestParam("deliverType") int deliverType);

    @PostMapping("/getContractDTOByForeignNo")
    Result<ElecContractDTO> getContractDTOByForeignNo(@RequestParam("foreignNo") String foreignNo);

    @PostMapping("/incrSendSmsCount")
    Result<Integer> incrSendSmsCount(@RequestParam("contractId") Long contractId);
}
