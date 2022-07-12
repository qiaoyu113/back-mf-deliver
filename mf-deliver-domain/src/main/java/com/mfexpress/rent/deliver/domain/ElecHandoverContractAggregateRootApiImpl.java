package com.mfexpress.rent.deliver.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import cn.hutool.json.JSONUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.base.starter.logback.log.PrintParam;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.ElecHandoverContractStatus;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
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
import com.mfexpress.rent.deliver.entity.ElecHandoverContract;
import com.mfexpress.rent.deliver.gateway.ElecHandoverContractGateway;
import com.mfexpress.rent.deliver.gateway.ElecHandoverDocGateway;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
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
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private BeanFactory beanFactory;

    @Resource
    private RedisTools redisTools;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource(name = "serveSyncServiceImpl")
    private EsSyncHandlerI serveSyncServiceI;


    @Override
    @PostMapping("/createDeliverContract")
    @Transactional(rollbackFor = Exception.class)
    @PrintParam
    public Result<ContractIdWithDocIds> createDeliverContract(@RequestBody @Validated CreateDeliverContractCmd cmd) {
        ElecHandoverContract elecHandoverContract = beanFactory.getBean(ElecHandoverContract.class);
        elecHandoverContract.init(cmd);
        elecHandoverContract.createCheck();
        // 从实体中得到将要持久化的合同对象
        ElectronicHandoverContractPO contractPO = elecHandoverContract.getContractPO();
        // 从实体中得到将要持久化的交接单对象
        List<ElectronicHandoverDocPO> docPOS = elecHandoverContract.getDocPOS();
        contractGateway.create(contractPO);
        Map<String, String> deliverNoWithDocId = docGateway.batchCreate(docPOS);

        ContractIdWithDocIds contractIdWithDocIds = new ContractIdWithDocIds();
        contractIdWithDocIds.setContractId(contractPO.getContractId());
        contractIdWithDocIds.setDeliverNoWithDocId(deliverNoWithDocId);
        return Result.getInstance(contractIdWithDocIds).success();
    }

    @Override
    @PostMapping("/createRecoverContract")
    @Transactional(rollbackFor = Exception.class)
    @PrintParam
    public Result<ContractIdWithDocIds> createRecoverContract(@RequestBody @Validated CreateRecoverContractCmd cmd) {
        ElecHandoverContract elecHandoverContract = beanFactory.getBean(ElecHandoverContract.class);
        elecHandoverContract.init(cmd);
        elecHandoverContract.createCheck();
        // 从实体中得到将要持久化的合同对象
        ElectronicHandoverContractPO contractPO = elecHandoverContract.getContractPO();
        // 从实体中得到将要持久化的交接单对象
        List<ElectronicHandoverDocPO> docPOS = elecHandoverContract.getDocPOS();
        contractGateway.create(contractPO);
        Map<String, String> deliverNoWithDocId = docGateway.batchCreate(docPOS);

        ContractIdWithDocIds contractIdWithDocIds = new ContractIdWithDocIds();
        contractIdWithDocIds.setContractId(contractPO.getContractId());
        contractIdWithDocIds.setDeliverNoWithDocId(deliverNoWithDocId);
        return Result.getInstance(contractIdWithDocIds).success();
    }

    @Override
    @PostMapping("/cancelContract")
    @PrintParam
    public Result<Integer> cancelContract(@RequestBody @Validated CancelContractCmd cmd) {
        ElecHandoverContract elecHandoverContract = beanFactory.getBean(ElecHandoverContract.class);
        elecHandoverContract.init(cmd);
        elecHandoverContract.cancelCheck();
        elecHandoverContract.cancel();
        return Result.getInstance(0).success();
    }

    // 来自契约锁的回调，合同创建中状态补充外部合同编号
    @Override
    @PostMapping("/completionContractForeignNo")
    @PrintParam
    public Result<Integer> completionContractForeignNo(@RequestBody @Validated ContractStatusChangeCmd cmd) {
        ElecHandoverContract elecHandoverContract = beanFactory.getBean(ElecHandoverContract.class);
        elecHandoverContract.init(cmd, null);
        // check 先不加
        elecHandoverContract.completionContractForeignNo();
        return Result.getInstance(0).success();
    }

    // 来自契约锁的回调，合同创建成功
    @Override
    @PostMapping("/signing")
    @PrintParam
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> signing(@RequestBody @Validated ContractStatusChangeCmd cmd) {
        ElecHandoverContract elecHandoverContract = beanFactory.getBean(ElecHandoverContract.class);
        elecHandoverContract.init(cmd, ElecHandoverContractStatus.SIGNING.getCode());
        elecHandoverContract.signingCheck();
        elecHandoverContract.signing();
        // redis存储该合同可发短信的倒计时
        String key = DeliverUtils.concatCacheKey(Constants.ELEC_CONTRACT_LAST_TIME_SEND_SMS_KEY, cmd.getContractId().toString());
        // 存活时间60s,时间单位是seconds
        redisTools.set(key, System.currentTimeMillis(), 60L);
        return Result.getInstance(0).success();
    }

    // 来自契约锁的回调，合同签署成功
    @Override
    @PostMapping("/completed")
    @PrintParam
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> completed(@RequestBody @Validated ContractStatusChangeCmd cmd) {
        ElecHandoverContract elecHandoverContract = beanFactory.getBean(ElecHandoverContract.class);
        elecHandoverContract.init(cmd, ElecHandoverContractStatus.COMPLETED.getCode());
        elecHandoverContract.completedCheck();
        elecHandoverContract.completed();
        elecHandoverContract.supplementDocPdfUrl(cmd.getDocPdfUrlMap());
        return Result.getInstance(0).success();
    }

    @Override
    @PostMapping("/fail")
    @PrintParam
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> fail(@RequestBody @Validated ContractStatusChangeCmd cmd) {
        ElecHandoverContract elecHandoverContract = beanFactory.getBean(ElecHandoverContract.class);
        elecHandoverContract.init(cmd, ElecHandoverContractStatus.FAIL.getCode());
        elecHandoverContract.failCheck();
        elecHandoverContract.fail();
        return Result.getInstance(0).success();
    }

    @Override
    @PostMapping("/confirmFailContract")
    @PrintParam
    public Result<Integer> confirmFailContract(@RequestBody @Validated ConfirmFailCmd cmd) {
        ElecHandoverContract elecHandoverContract = beanFactory.getBean(ElecHandoverContract.class);
        elecHandoverContract.init(cmd);
        elecHandoverContract.confirmFailContractCheck();
        elecHandoverContract.confirmFailContract();
        return Result.getInstance(0).success();
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
        ElectronicHandoverContractPO contractPO = contractGateway.getContractByContractId(contractId);
        if (null == contractPO) {
            return Result.getInstance((Integer) null).fail(ResultErrorEnum.OPER_ERROR.getCode(), "电子交接单不存在");
        }

        ElectronicHandoverContractPO contractPOToUpdate = new ElectronicHandoverContractPO();
        contractPOToUpdate.setContractId(contractPO.getContractId());
        contractPOToUpdate.setSendSmsDate(FormatUtil.ymdHmsFormatDateToString(new Date()));
        if (ElecHandoverContractStatus.SIGNING.getCode() == contractPO.getStatus()) {
            if(StringUtils.isEmpty(contractPO.getSendSmsDate())){
                contractPOToUpdate.setSendSmsCount(1);
            }else{
                String sendSmsDate = contractPO.getSendSmsDate().substring(0, 10);
                String nowYmd = FormatUtil.ymdFormatDateToString(new Date());
                if (nowYmd.equals(sendSmsDate)) {
                    // 如果是今天，发送短信次数加1
                    contractPOToUpdate.setSendSmsCount(contractPO.getSendSmsCount() + 1);
                } else {
                    // 如果不是今天，发送短信次数设为1，并将日期改为今天
                    contractPOToUpdate.setSendSmsCount(1);
                }
            }
            contractPOToUpdate.setSendSmsDate(FormatUtil.ymdHmsFormatDateToString(new Date()));
            contractGateway.updateContractByContractId(contractPOToUpdate);
        }

        return Result.getInstance(0).success();
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


        ElecHandoverContract elecHandoverContract = beanFactory.getBean(ElecHandoverContract.class);
        elecHandoverContract.init(cmd, ElecHandoverContractStatus.COMPLETED.getCode());
        // 先不加check
        elecHandoverContract.autoCompleted();

        return Result.getInstance(0).success();
    }
}
