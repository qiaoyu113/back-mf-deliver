package com.mfexpress.rent.deliver.entity;

import cn.hutool.json.JSONUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.*;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ContractIdWithDocIds;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverImgInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.RecoverInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.po.ElectronicHandoverContractPO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.po.ElectronicHandoverDocPO;
import com.mfexpress.rent.deliver.dto.entity.Deliver;
import com.mfexpress.rent.deliver.entity.api.ElecHandoverContractEntityApi;
import com.mfexpress.rent.deliver.gateway.DeliverGateway;
import com.mfexpress.rent.deliver.gateway.ElecHandoverContractGateway;
import com.mfexpress.rent.deliver.gateway.ElecHandoverDocGateway;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 电子交接合同实体
 */
@Data
@Component
@Scope("prototype")
@Slf4j
public class ElecHandoverContractEntity implements ElecHandoverContractEntityApi {

    // 唯一id
    private Long contractId;

    // 值对象
    // 发车交付信息
    private DeliverInfo deliverInfo;

    // 收车交付信息
    private RecoverInfo recoverInfo;

    // 合同所管理的交接单
    private List<String> deliverNos;

    // 车牌号和人车合照组合列表
    private List<DeliverImgInfo> deliverImgInfos;

    // 交接类型，1：发车，2：收车
    private Integer deliverType;

    // 第三方的电子合同编号
    private String contractForeignNo;

    // 状态
    private Integer status;

    // 失败原因
    private Integer failureReason;

    // 具体的失败原因描述
    private String failureMsg;

    // 所属订单
    private Long orderId;

    // 所属大区
    private Integer orgId;

    // 所属城市
    private Integer cityId;

    // 当合同在末端状态时（过期、成功？、生成失败？、其他错误？）是否在签署中列表展示，1：是，0：否
    private Integer isShow;

    // 发送短信次数(结合发送短信日期字段一起看，例如：某某日发了几次短信)
    private Integer sendSmsCount;

    // 发送短信日期
    private Date sendSmsDate;

    // 创建人id
    private Integer creatorId;

    // 更新人id
    private Integer updaterId;

    // 原始数据/数据库中的原始数据
    private ElectronicHandoverContractPO contractPO;

    @Resource
    private RedisTools redisTools;

    @Resource
    private ElecHandoverContractGateway elecHandoverContractGateway;

    @Resource
    private ElecHandoverDocGateway elecHandoverDocGateway;

    @Resource
    private DeliverGateway deliverGateway;

    @Resource
    private ElecHandoverContractGateway contractGateway;

    @Resource
    private ElecHandoverDocGateway docGateway;

    public ElecHandoverContractEntity(){}

    public ElecHandoverContractEntity(ElecHandoverContractEntity entity) {
        this.redisTools = entity.redisTools;
        this.elecHandoverContractGateway = entity.elecHandoverContractGateway;
        this.elecHandoverDocGateway = entity.elecHandoverDocGateway;
        this.deliverGateway = entity.deliverGateway;
        this.contractGateway = entity.contractGateway;
        this.docGateway = entity.docGateway;
    }

    // 创建电子合同时的数据初始化
    public void init(CreateElecHandoverContractCmd cmd) {
        this.deliverType = cmd.getDeliverType();
        if (DeliverTypeEnum.DELIVER.getCode() == deliverType) {
            CreateDeliverContractCmd deliverContractCmd = (CreateDeliverContractCmd) cmd;
            this.deliverInfo = deliverContractCmd.getDeliverInfo();
        } else if (DeliverTypeEnum.RECOVER.getCode() == deliverType) {
            CreateRecoverContractCmd recoverContractCmd = (CreateRecoverContractCmd) cmd;
            this.recoverInfo = recoverContractCmd.getRecoverInfo();
        }
        // 入参是否自动校验了该参数，需要确认
        this.deliverImgInfos = cmd.getDeliverImgInfos();
        this.deliverNos = deliverImgInfos.stream().map(DeliverImgInfo::getDeliverNo).collect(Collectors.toList());
        this.status = ElecHandoverContractStatus.GENERATING.getCode();
        // orgid cityid createid
        this.orderId = cmd.getOrderId();
        this.orgId = cmd.getOrgId();
        this.creatorId = cmd.getOperatorId();
        this.updaterId = cmd.getOperatorId();
    }

    // 取消合同时的初始化
    public void init(CancelContractCmd cmd) {
        this.contractId = cmd.getContractId();
        this.status = ElecHandoverContractStatus.FAIL.getCode();
        this.failureReason = cmd.getFailureReason();
        this.updaterId = cmd.getOperatorId();
    }

    // 合同状态被动改变(创建成功、签署完成、过期等)命令的初始化
    public void init(ContractStatusChangeCmd cmd, Integer status) {
        this.contractId = cmd.getContractId();
        this.contractForeignNo = cmd.getContractForeignNo();
        this.status = status;
        this.failureReason = cmd.getFailureReason();
        this.failureMsg = cmd.getFailureMsg();
    }

    // 用户确认合同过期命令的初始化
    public void init(ConfirmFailCmd cmd) {
        this.contractId = cmd.getContractId();
        this.isShow = JudgeEnum.NO.getCode();
        this.updaterId = cmd.getOperatorId();
    }

    public void createCheck() {

    }

    public void cancelCheck() {
        // 数据存在？ 数据已被取消？
        ElectronicHandoverContractPO contractPO = elecHandoverContractGateway.getContractByContractId(contractId);
        if (null == contractPO) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "获取电子交接单失败");
        }
        if (ElecHandoverContractStatus.COMPLETED.getCode() == contractPO.getStatus()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "电子交接单状态已被签署，不可取消");
        }
        /*if(ElecHandoverContractStatus.FAIL.getCode() == contractPO.getStatus()){
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "电子交接单已在失败状态，不可取消");
        }*/
        /*if(ElecHandoverContractStatus.GENERATING.getCode() != contractPO.getStatus() && ElecHandoverContractStatus.SIGNING.getCode() != contractPO.getStatus()){
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "电子交接单状态异常");
        }*/
    }

    public ElectronicHandoverContractPO getContractPO() {
        ElectronicHandoverContractPO po = new ElectronicHandoverContractPO();
        BeanUtils.copyProperties(this, po);
        po.setDeliverNos(JSONUtil.toJsonStr(deliverNos));
        po.setPlateNumberWithImgs(JSONUtil.toJsonStr(deliverImgInfos));
        if (DeliverTypeEnum.DELIVER.getCode() == deliverType) {
            // 联系人姓名、手机号、身份证号被copy
            BeanUtils.copyProperties(deliverInfo, po);
            po.setDeliverVehicleTime(FormatUtil.ymdHmsFormatDateToString(deliverInfo.getDeliverVehicleTime()));
        } else if (DeliverTypeEnum.RECOVER.getCode() == deliverType) {
            // 联系人姓名、手机号、身份证号被copy
            BeanUtils.copyProperties(recoverInfo, po);
            po.setRecoverVehicleTime(FormatUtil.ymdHmsFormatDateToString(recoverInfo.getRecoverVehicleTime()));
            po.setRecoverDamageFee(recoverInfo.getDamageFee());
            po.setRecoverParkFee(recoverInfo.getParkFee());
            po.setRecoverWareHouseId(recoverInfo.getWareHouseId());
        }

        contractId = redisTools.getBizId(125);
        po.setContractId(contractId);
        long incr = redisTools.incr(DeliverUtils.getEnvVariable(Constants.REDIS_DELIVER_CONTRACT_KEY) + DeliverUtils.getDateByYYMMDD(new Date()), 1);
        po.setContractShowNo(DeliverUtils.getNo(Constants.REDIS_DELIVER_CONTRACT_KEY, incr));
        return po;
    }

    public List<ElectronicHandoverDocPO> getDocPOS() {
        return deliverNos.stream().map(deliverNO -> {
            ElectronicHandoverDocPO docPO = new ElectronicHandoverDocPO();
            docPO.setContractId(contractId);
            docPO.setDeliverNo(deliverNO);
            docPO.setDeliverType(deliverType);
            docPO.setValidStatus(JudgeEnum.YES.getCode());
            return docPO;
        }).collect(Collectors.toList());
    }

    // 合同失效操作
    public void cancel() {
        // 将合同设为失效
        ElectronicHandoverContractPO contractPO = new ElectronicHandoverContractPO();
        contractPO.setContractId(contractId);
        contractPO.setStatus(status);
        contractPO.setFailureReason(failureReason);
        contractPO.setUpdaterId(updaterId);
        elecHandoverContractGateway.updateContractByContractId(contractPO);
        // 将交接单设为失效
        ElectronicHandoverDocPO docPO = new ElectronicHandoverDocPO();
        docPO.setContractId(contractId);
        docPO.setValidStatus(JudgeEnum.NO.getCode());
        elecHandoverDocGateway.updateDocByDoc(docPO);
    }

    // 补充合同的三方编号操作
    public void completionContractForeignNo() {
        ElectronicHandoverContractPO contractPO = new ElectronicHandoverContractPO();
        contractPO.setContractId(contractId);
        contractPO.setContractForeignNo(contractForeignNo);
        elecHandoverContractGateway.updateContractByContractId(contractPO);
    }

    // 接收到合同创建成功命令后的检查操作
    public void signingCheck() {
        ElectronicHandoverContractPO contractQryPO = new ElectronicHandoverContractPO();
        contractQryPO.setContractId(contractId);
        contractPO = elecHandoverContractGateway.getContractByContract(contractQryPO);
        if (null == contractPO || ElecHandoverContractStatus.GENERATING.getCode() != contractPO.getStatus()) {
            log.error("接收到合同创建成功命令后的检查操作，合同不存在或者合同状态不符合预期，合同id：{}", contractForeignNo);
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "修改合同状态失败");
        }
        // 暂不校验交付单状态
        //List<String> deliverNos = JSONUtil.toList(contractPO.getDeliverNos(), String.class);
    }

    // 更改合同状态为创建成功/签署中的操作
    public void signing() {
        ElectronicHandoverContractPO contractPOToUpdate = new ElectronicHandoverContractPO();
        contractPOToUpdate.setContractId(contractId);
        //contractPOToUpdate.setContractForeignNo(contractForeignNo);
        contractPOToUpdate.setStatus(status);
        elecHandoverContractGateway.updateContractByContractId(contractPOToUpdate);
    }

    // 接收到合同签署完成命令后的检查操作
    public void completedCheck() {
        ElectronicHandoverContractPO contractQryPO = new ElectronicHandoverContractPO();
        contractQryPO.setContractForeignNo(contractForeignNo);
        contractPO = elecHandoverContractGateway.getContractByContract(contractQryPO);
        if (null == contractPO || ElecHandoverContractStatus.SIGNING.getCode() != contractPO.getStatus()) {
            log.error("接收到合同签署完成命令后的检查操作，合同不存在或者合同状态不符合预期，合同id：{}", contractForeignNo);
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "修改合同状态失败");
        }
        // 暂不校验交付单状态
        //List<String> deliverNos = JSONUtil.toList(contractPO.getDeliverNos(), String.class);
    }

    // 更改合同状态为完成的操作
    public void completed() {
        ElectronicHandoverContractPO contractPOToUpdate = new ElectronicHandoverContractPO();
        contractPOToUpdate.setContractForeignNo(contractForeignNo);
        contractPOToUpdate.setStatus(status);
        elecHandoverContractGateway.updateContractByContractForeignNo(contractPOToUpdate);
    }

    public void autoCompleted() {
        ElectronicHandoverContractPO contractPOToUpdate = new ElectronicHandoverContractPO();
        contractPOToUpdate.setContractId(contractId);
        contractPOToUpdate.setContractForeignNo(contractForeignNo);
        contractPOToUpdate.setStatus(status);
        log.info("contractPOToUpdate---->{}", contractPOToUpdate);
        elecHandoverContractGateway.updateContractByContractId(contractPOToUpdate);
    }

    // 补充交接单的文件url
    public void supplementDocPdfUrl(Map<String, String> docPdfUrlMap) {
        // 多次访问数据库？
        ElectronicHandoverDocPO docPO = new ElectronicHandoverDocPO();
        Set<String> keys = docPdfUrlMap.keySet();
        keys.forEach(key -> {
            docPO.setFileUrl(docPdfUrlMap.get(key));
            elecHandoverDocGateway.updateDocByDocId(Integer.valueOf(key), docPO);
        });
    }

    // 接收到合同失败命令后的检查操作
    public void failCheck() {
        ElectronicHandoverContractPO contractQryPO = new ElectronicHandoverContractPO();
        contractQryPO.setContractId(contractId);
        contractPO = elecHandoverContractGateway.getContractByContract(contractQryPO);
        // SIGNING 和 GENERATING 都可以被改为失败，判断条件需要改变吗？待确认
        if (null == contractPO || (ElecHandoverContractStatus.SIGNING.getCode() != contractPO.getStatus() && ElecHandoverContractStatus.GENERATING.getCode() != contractPO.getStatus())) {
            log.error("接收到合同失败命令后的检查操作，合同不存在或合同状态不在生成中或签署中，合同id：{}", contractForeignNo);
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "修改合同状态失败");
        }
    }

    // 更改合同状态为失败的操作
    public void fail() {
        ElectronicHandoverContractPO contractPOToUpdate = new ElectronicHandoverContractPO();
        contractPOToUpdate.setContractId(contractId);
        contractPOToUpdate.setStatus(status);
        contractPOToUpdate.setFailureReason(failureReason);
        contractPOToUpdate.setFailureMsg(failureMsg);
        // 失败原因为过期或者创建失败，合同仍需展示
        if (ContractFailureReasonEnum.CREATE_FAIL.getCode() == failureReason || ContractFailureReasonEnum.OVERDUE.getCode() == failureReason) {
            contractPOToUpdate.setIsShow(JudgeEnum.YES.getCode());
        }
        elecHandoverContractGateway.updateContractByContractId(contractPOToUpdate);
        // 电子交接单也需置为失效状态
        ElectronicHandoverDocPO docPO = new ElectronicHandoverDocPO();
        docPO.setContractId(contractPO.getContractId());
        docPO.setValidStatus(JudgeEnum.NO.getCode());
        elecHandoverDocGateway.updateDocByDoc(docPO);
    }

    // 接收到确认合同过期命令后的检查操作
    public void confirmFailContractCheck() {
        ElectronicHandoverContractPO contractQryPO = new ElectronicHandoverContractPO();
        contractQryPO.setContractId(contractId);
        contractPO = elecHandoverContractGateway.getContractByContract(contractQryPO);
        if (null == contractPO) {
            log.error("接收到确认合同失败命令后的检查操作，合同不存在，合同id：{}", contractForeignNo);
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "合同查询失败");
        }
        if (ElecHandoverContractStatus.FAIL.getCode() != contractPO.getStatus() || (ContractFailureReasonEnum.OVERDUE.getCode() != contractPO.getFailureReason() && ContractFailureReasonEnum.CREATE_FAIL.getCode() != contractPO.getFailureReason())
                || !JudgeEnum.YES.getCode().equals(contractPO.getIsShow())) {
            log.error("接收到确认合同失败命令后的检查操作，合同状态异常，合同id：{}", contractForeignNo);
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "合同状态异常");
        }
    }

    // 接收到确认合同过期命令后的更改合同isShow字段的操作
    public void confirmFailContract() {
        ElectronicHandoverContractPO contractPO = new ElectronicHandoverContractPO();
        contractPO.setContractId(contractId);
        contractPO.setIsShow(JudgeEnum.NO.getCode());
        elecHandoverContractGateway.updateContractByContractId(contractPO);
    }

    @Override
    public ContractIdWithDocIds createDeliverContract(CreateDeliverContractCmd cmd) {
        ElecHandoverContractEntity elecHandoverContractEntity = new ElecHandoverContractEntity(this);
        elecHandoverContractEntity.init(cmd);
        elecHandoverContractEntity.createCheck();
        // 从实体中得到将要持久化的合同对象
        ElectronicHandoverContractPO contractPO = elecHandoverContractEntity.getContractPO();
        // 从实体中得到将要持久化的交接单对象
        List<ElectronicHandoverDocPO> docPOS = elecHandoverContractEntity.getDocPOS();
        contractGateway.create(contractPO);
        Map<String, String> deliverNoWithDocId = docGateway.batchCreate(docPOS);

        ContractIdWithDocIds contractIdWithDocIds = new ContractIdWithDocIds();
        contractIdWithDocIds.setContractId(contractPO.getContractId());
        contractIdWithDocIds.setDeliverNoWithDocId(deliverNoWithDocId);
        return contractIdWithDocIds;
    }

    @Override
    public ContractIdWithDocIds createRecoverContract(CreateRecoverContractCmd cmd) {
        ElecHandoverContractEntity elecHandoverContractEntity = new ElecHandoverContractEntity(this);
        elecHandoverContractEntity.init(cmd);
        elecHandoverContractEntity.createCheck();
        // 从实体中得到将要持久化的合同对象
        ElectronicHandoverContractPO contractPO = elecHandoverContractEntity.getContractPO();
        // 从实体中得到将要持久化的交接单对象
        List<ElectronicHandoverDocPO> docPOS = elecHandoverContractEntity.getDocPOS();
        contractGateway.create(contractPO);
        Map<String, String> deliverNoWithDocId = docGateway.batchCreate(docPOS);

        ContractIdWithDocIds contractIdWithDocIds = new ContractIdWithDocIds();
        contractIdWithDocIds.setContractId(contractPO.getContractId());
        contractIdWithDocIds.setDeliverNoWithDocId(deliverNoWithDocId);
        return contractIdWithDocIds;
    }

    @Override
    public Integer cancelContract(CancelContractCmd cmd) {
        ElecHandoverContractEntity elecHandoverContractEntity = new ElecHandoverContractEntity(this);
        elecHandoverContractEntity.init(cmd);
        elecHandoverContractEntity.cancelCheck();
        elecHandoverContractEntity.cancel();
        return 0;
    }

    @Override
    public Integer completionContractForeignNo(ContractStatusChangeCmd cmd) {
        ElecHandoverContractEntity elecHandoverContractEntity = new ElecHandoverContractEntity(this);
        elecHandoverContractEntity.init(cmd, null);
        // check 先不加
        elecHandoverContractEntity.completionContractForeignNo();
        return 0;
    }

    @Override
    public Integer signing(ContractStatusChangeCmd cmd) {
        ElecHandoverContractEntity elecHandoverContractEntity = new ElecHandoverContractEntity(this);
        elecHandoverContractEntity.init(cmd, ElecHandoverContractStatus.SIGNING.getCode());
        elecHandoverContractEntity.signingCheck();
        elecHandoverContractEntity.signing();
        // redis存储该合同可发短信的倒计时
        String key = DeliverUtils.concatCacheKey(Constants.ELEC_CONTRACT_LAST_TIME_SEND_SMS_KEY, cmd.getContractId().toString());
        // 存活时间60s,时间单位是seconds
        redisTools.set(key, System.currentTimeMillis(), 60L);
        return 0;
    }

    @Override
    public Integer completed(ContractStatusChangeCmd cmd) {
        ElecHandoverContractEntity elecHandoverContractEntity = new ElecHandoverContractEntity(this);
        elecHandoverContractEntity.init(cmd, ElecHandoverContractStatus.COMPLETED.getCode());
        elecHandoverContractEntity.completedCheck();
        elecHandoverContractEntity.completed();
        elecHandoverContractEntity.supplementDocPdfUrl(cmd.getDocPdfUrlMap());
        return 0;
    }

    @Override
    public Integer fail(ContractStatusChangeCmd cmd) {
        ElecHandoverContractEntity elecHandoverContractEntity = new ElecHandoverContractEntity(this);
        elecHandoverContractEntity.init(cmd, ElecHandoverContractStatus.FAIL.getCode());
        elecHandoverContractEntity.failCheck();
        elecHandoverContractEntity.fail();
        return 0;
    }

    @Override
    public Integer confirmFailContract(ConfirmFailCmd cmd) {
        ElecHandoverContractEntity elecHandoverContractEntity = new ElecHandoverContractEntity(this);
        elecHandoverContractEntity.init(cmd);
        elecHandoverContractEntity.confirmFailContractCheck();
        elecHandoverContractEntity.confirmFailContract();
        return 0;
    }

    @Override
    public Integer incrSendSmsCount(Long contractId) {
        ElectronicHandoverContractPO contractPO = contractGateway.getContractByContractId(contractId);
        if (null == contractPO) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "电子交接单不存在");
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
        return 0;
    }

    @Override
    public Integer autoCompleted(ContractStatusChangeCmd cmd) {
        ElecHandoverContractEntity elecHandoverContractEntity = new ElecHandoverContractEntity(this);
        elecHandoverContractEntity.init(cmd, ElecHandoverContractStatus.COMPLETED.getCode());
        // 先不加check
        elecHandoverContractEntity.autoCompleted();
        return 0;
    }

}
