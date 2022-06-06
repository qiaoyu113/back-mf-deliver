package com.mfexpress.rent.deliver.domainapi;

import java.util.List;
import java.util.Map;

import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CancelContractCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.ConfirmFailCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.ContractStatusChangeCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CreateDeliverContractCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CreateRecoverContractCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ContractIdWithDocIds;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecDocDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractListQry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

    @PostMapping("/confirmFailContract")
    Result<Integer> confirmFailContract(@RequestBody @Validated ConfirmFailCmd cmd);

    @PostMapping("/getContractDTOSByDeliverNosAndDeliverType")
    Result<List<ElecContractDTO>> getContractDTOSByDeliverNosAndDeliverType(@RequestParam("deliverNos") List<String> deliverNos, @RequestParam("deliverType") int deliverType);

    @PostMapping("/getContractDTOByForeignNo")
    Result<ElecContractDTO> getContractDTOByForeignNo(@RequestParam("foreignNo") String foreignNo);

    @PostMapping("/incrSendSmsCount")
    Result<Integer> incrSendSmsCount(@RequestParam("contractId") Long contractId);

    @PostMapping("/getContractDTOByDeliverNoAndDeliverType")
    Result<ElecContractDTO> getContractDTOByDeliverNoAndDeliverType(@RequestParam("deliverNo") String deliverNo, @RequestParam("deliverType") Integer deliverType);

    @PostMapping("/getDocDTOByDeliverNoAndDeliverType")
    Result<ElecDocDTO> getDocDTOByDeliverNoAndDeliverType(@RequestParam("deliverNo") String deliverNo, @RequestParam("deliverType") Integer deliverType);

    @PostMapping("/getContractIdMapByQry")
    Result<Map<String, String>> getContractIdMapByQry(@RequestBody ContractListQry contractListQry);

    @PostMapping("/getDocDTOByContractId")
    Result<ElecDocDTO> getDocDTOByContractId(@RequestParam("contractId") Long contractId);

    @PostMapping(value = "/completed/auto")
    Result<Integer> autoCompleted(@RequestBody @Validated ContractStatusChangeCmd cmd);
}
