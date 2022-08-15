package com.mfexpress.rent.deliver.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import cn.hutool.json.JSONUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.constant.ElecHandoverContractStatus;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CancelContractCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.ConfirmFailCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.ContractStatusChangeCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CreateDeliverContractCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CreateRecoverContractCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ContractIdWithDocIds;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecDocDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.po.ElectronicHandoverContractPO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.po.ElectronicHandoverDocPO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractListQry;
import com.mfexpress.rent.deliver.entity.ElecHandoverContractEntity;
import com.mfexpress.rent.deliver.entity.api.ElecHandoverContractEntityApi;
import com.mfexpress.rent.deliver.gateway.ElecHandoverContractGateway;
import com.mfexpress.rent.deliver.gateway.ElecHandoverDocGateway;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/domain/deliver/v3/elecHandoverContract")
@Api(tags = "domain--交付--电子交接合同聚合", value = "ElecHandoverContractAggregateRootApiImpl")
@RefreshScope
public class ElecHandoverContractAggregateRootApiImpl implements ElecHandoverContractAggregateRootApi {

    @Resource
    private ElecHandoverContractGateway contractGateway;

    @Resource
    private ElecHandoverDocGateway docGateway;

    @Resource
    private BeanFactory beanFactory;

    @Resource
    private ElecHandoverContractEntityApi elecHandoverContractEntityApi;

    @Override
    @PostMapping("/createDeliverContract")
    @Transactional(rollbackFor = Exception.class)
    @PrintParam
    public Result<ContractIdWithDocIds> createDeliverContract(@RequestBody @Validated CreateDeliverContractCmd cmd) {
        return Result.getInstance(elecHandoverContractEntityApi.createDeliverContract(cmd)).success();
    }

    @Override
    @PostMapping("/createRecoverContract")
    @Transactional(rollbackFor = Exception.class)
    @PrintParam
    public Result<ContractIdWithDocIds> createRecoverContract(@RequestBody @Validated CreateRecoverContractCmd cmd) {
        return Result.getInstance(elecHandoverContractEntityApi.createRecoverContract(cmd)).success();
    }

    @Override
    @PostMapping("/cancelContract")
    @PrintParam
    public Result<Integer> cancelContract(@RequestBody @Validated CancelContractCmd cmd) {
        return Result.getInstance(elecHandoverContractEntityApi.cancelContract(cmd)).success();
    }

    // 来自契约锁的回调，合同创建中状态补充外部合同编号
    @Override
    @PostMapping("/completionContractForeignNo")
    @PrintParam
    public Result<Integer> completionContractForeignNo(@RequestBody @Validated ContractStatusChangeCmd cmd) {
        return Result.getInstance(elecHandoverContractEntityApi.completionContractForeignNo(cmd)).success();
    }

    // 来自契约锁的回调，合同创建成功
    @Override
    @PostMapping("/signing")
    @PrintParam
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> signing(@RequestBody @Validated ContractStatusChangeCmd cmd) {
        return Result.getInstance(elecHandoverContractEntityApi.signing(cmd)).success();
    }

    // 来自契约锁的回调，合同签署成功
    @Override
    @PostMapping("/completed")
    @PrintParam
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> completed(@RequestBody @Validated ContractStatusChangeCmd cmd) {
        return Result.getInstance(elecHandoverContractEntityApi.completed(cmd)).success();
    }

    @Override
    @PostMapping("/fail")
    @PrintParam
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> fail(@RequestBody @Validated ContractStatusChangeCmd cmd) {
        return Result.getInstance(elecHandoverContractEntityApi.fail(cmd)).success();
    }

    @Override
    @PostMapping("/confirmFailContract")
    @PrintParam
    public Result<Integer> confirmFailContract(@RequestBody @Validated ConfirmFailCmd cmd) {
        return Result.getInstance(elecHandoverContractEntityApi.confirmFailContract(cmd)).success();
    }

    @Override
    @PostMapping("/getContractDTOByContractId")
    @PrintParam
    public Result<ElecContractDTO> getContractDTOByContractId(@RequestParam("contractId") Long contractId) {
        ElectronicHandoverContractPO contractPO = contractGateway.getContractByContractId(contractId);
        if(null == contractPO){
            return Result.getInstance((ElecContractDTO)null).success();
        }
        ElecContractDTO elecContractDTO = new ElecContractDTO();
        BeanUtils.copyProperties(contractPO, elecContractDTO);
        elecContractDTO.setDeliverVehicleTime(FormatUtil.ymdHmsFormatStringToDate(contractPO.getDeliverVehicleTime()));
        elecContractDTO.setRecoverVehicleTime(FormatUtil.ymdHmsFormatStringToDate(contractPO.getRecoverVehicleTime()));
        return Result.getInstance(elecContractDTO).success();
    }

    @Override
    @PostMapping("/getPageContractDTOSByQry")
    @PrintParam
    public PagePagination<ElecContractDTO> getPageContractDTOSByQry(@RequestBody @Validated ContractListQry qry) {
        PagePagination<ElectronicHandoverContractPO> pagePagination = contractGateway.getPageContractDTOSByQry(qry);
        List<ElectronicHandoverContractPO> poList = pagePagination.getList();
        List<ElecContractDTO> contractDTOS = poList.stream().map(contractPO -> {
            ElecContractDTO contractDTO = new ElecContractDTO();
            BeanUtils.copyProperties(contractPO, contractDTO);
            contractDTO.setDeliverVehicleTime(FormatUtil.ymdHmsFormatStringToDate(contractPO.getDeliverVehicleTime()));
            contractDTO.setRecoverVehicleTime(FormatUtil.ymdHmsFormatStringToDate(contractPO.getRecoverVehicleTime()));
            return contractDTO;
        }).collect(Collectors.toList());

        PagePagination<ElecContractDTO> pagePaginationDTO = new PagePagination<>();
        pagePaginationDTO.setPage(pagePagination.getPage());
        pagePaginationDTO.setPagination(pagePagination.getPagination());
        pagePaginationDTO.setList(contractDTOS);
        return pagePaginationDTO;
    }

    @Override
    @PostMapping("/getContractDTOSByDeliverNosAndDeliverType")
    @PrintParam
    public Result<List<ElecContractDTO>> getContractDTOSByDeliverNosAndDeliverType(@RequestParam("deliverNos") List<String> deliverNos, @RequestParam("deliverType") int deliverType) {
        List<ElectronicHandoverContractPO> contractPOS =  contractGateway.getContractDTOSByDeliverNosAndDeliverType(deliverNos, deliverType);
        List<ElecContractDTO> contractDTOS = contractPOS.stream().map(contractPO -> {
            ElecContractDTO contractDTO = new ElecContractDTO();
            BeanUtils.copyProperties(contractPO, contractDTO);
            contractDTO.setDeliverVehicleTime(FormatUtil.ymdHmsFormatStringToDate(contractPO.getDeliverVehicleTime()));
            contractDTO.setRecoverVehicleTime(FormatUtil.ymdHmsFormatStringToDate(contractPO.getRecoverVehicleTime()));
            return contractDTO;
        }).collect(Collectors.toList());
        return Result.getInstance(contractDTOS).success();
    }

    @Override
    @PostMapping("/getContractDTOByForeignNo")
    @PrintParam
    public Result<ElecContractDTO> getContractDTOByForeignNo(@RequestParam("foreignNo") String foreignNo) {
        ElectronicHandoverContractPO contractPO = contractGateway.getContractByForeignNo(foreignNo);
        if(null == contractPO){
            return Result.getInstance((ElecContractDTO)null).success();
        }
        ElecContractDTO elecContractDTO = new ElecContractDTO();
        BeanUtils.copyProperties(contractPO, elecContractDTO);
        elecContractDTO.setDeliverVehicleTime(FormatUtil.ymdHmsFormatStringToDate(contractPO.getDeliverVehicleTime()));
        elecContractDTO.setRecoverVehicleTime(FormatUtil.ymdHmsFormatStringToDate(contractPO.getRecoverVehicleTime()));
        return Result.getInstance(elecContractDTO).success();
    }

    @Override
    @PostMapping("/incrSendSmsCount")
    @PrintParam
    public Result<Integer> incrSendSmsCount(@RequestParam("contractId") Long contractId) {
        return Result.getInstance(elecHandoverContractEntityApi.incrSendSmsCount(contractId)).success();
    }

    @Override
    @PostMapping("/getContractDTOByDeliverNoAndDeliverType")
    @PrintParam
    public Result<ElecContractDTO> getContractDTOByDeliverNoAndDeliverType(@RequestParam("deliverNo") String deliverNo, @RequestParam("deliverType") Integer deliverType) {
        if (StringUtils.isEmpty(deliverNo) || null == deliverType) {
            return Result.getInstance((ElecContractDTO)null).fail(-1, "参数不可为空");
        }
        List<ElectronicHandoverContractPO> contractPOS =  contractGateway.getContractDTOSByDeliverNoAndDeliverType(deliverNo, deliverType);
        if(contractPOS.isEmpty()){
            return Result.getInstance((ElecContractDTO)null).success();
        }
        // 取最新的电子交接合同
        ElectronicHandoverContractPO contractPO = contractPOS.get(0);
        ElecContractDTO contractDTO = new ElecContractDTO();
        BeanUtils.copyProperties(contractPO, contractDTO);
        contractDTO.setDeliverVehicleTime(FormatUtil.ymdHmsFormatStringToDate(contractPO.getDeliverVehicleTime()));
        contractDTO.setRecoverVehicleTime(FormatUtil.ymdHmsFormatStringToDate(contractPO.getRecoverVehicleTime()));
        return Result.getInstance(contractDTO).success();
    }

    @Override
    @PostMapping("/getDocDTOByDeliverNoAndDeliverType")
    @PrintParam
    public Result<ElecDocDTO> getDocDTOByDeliverNoAndDeliverType(@RequestParam("deliverNo") String deliverNo, @RequestParam("deliverType") Integer deliverType) {
        if (StringUtils.isEmpty(deliverNo) || null == deliverType) {
            return Result.getInstance((ElecDocDTO)null).fail(-1, "参数不可为空");
        }
        ElectronicHandoverDocPO docPO = docGateway.getDocByDeliverNoAndDeliverType(deliverNo, deliverType);
        if(null == docPO){
            return Result.getInstance((ElecDocDTO)null).success();
        }
        ElecDocDTO elecDocDTO = new ElecDocDTO();
        BeanUtils.copyProperties(docPO, elecDocDTO);
        return Result.getInstance(elecDocDTO).success();
    }

    @Override
    @PostMapping("/getContractIdMapByQry")
    @PrintParam
    public Result<Map<String, String>> getContractIdMapByQry(@RequestBody ContractListQry qry) {
        List<ElecContractDTO> contractDTOS = contractGateway.getContractDTOSByQry(qry);
        Map<String, String> allContractIdMap = new HashMap<>();
        contractDTOS.forEach(contractDTO -> {
            List<String> deliverNos = JSONUtil.toList(contractDTO.getDeliverNos(), String.class);
            deliverNos.forEach(deliverNo -> {
                allContractIdMap.put(deliverNo, contractDTO.getContractId().toString());
            });
        });
        Map<String, String> neededContractIdMap = new HashMap<>();
        List<String> deliverNos = qry.getDeliverNos();
        deliverNos.forEach(deliverNo -> {
            neededContractIdMap.put(deliverNo, allContractIdMap.get(deliverNo));
        });
        return Result.getInstance(neededContractIdMap).success();
    }

    @Override
    public Result<ElecDocDTO> getDocDTOByContractId(Long contractId) {
        if (null == contractId) {
            return Result.getInstance((ElecDocDTO)null).fail(-1, "参数不可为空");
        }
        ElectronicHandoverDocPO docPO = docGateway.getDocByContractId(contractId);
        if(null == docPO){
            return Result.getInstance((ElecDocDTO)null).success();
        }
        ElecDocDTO elecDocDTO = new ElecDocDTO();
        BeanUtils.copyProperties(docPO, elecDocDTO);
        return Result.getInstance(elecDocDTO).success();
    }

    @Override
    @PostMapping(value = "/completed/auto")
    @Transactional(rollbackFor = Exception.class)
    @PrintParam
    public Result<Integer> autoCompleted(@RequestBody @Validated ContractStatusChangeCmd cmd) {
        return Result.getInstance(elecHandoverContractEntityApi.autoCompleted(cmd)).success();
    }

}
