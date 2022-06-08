package com.mfexpress.rent.deliver.entity;

import cn.hutool.json.JSONUtil;
import com.mfexpress.component.starter.tools.mq.MqTools;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ServeAdjustChargeRentTypeEnum;
import com.mfexpress.rent.deliver.constant.ServeChangeRecordEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCmd;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositListDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDepositDTO;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeCancelCmd;
import com.mfexpress.rent.deliver.entity.api.ServeEntityApi;
import com.mfexpress.rent.deliver.gateway.ServeAdjustRecordGateway;
import com.mfexpress.rent.deliver.gateway.ServeChangeRecordGateway;
import com.mfexpress.rent.deliver.gateway.ServeGateway;
import com.mfexpress.rent.deliver.po.ServeAdjustRecordPO;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import java.math.BigDecimal;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "serve")
@Builder
@Component
@Slf4j
public class ServeEntity implements ServeEntityApi {

    @Id
    private Integer id;

    private Long orderId;

    private Long serveId;

    private Integer customerId;

    private String serveNo;

    private Integer carModelId;

    private Integer leaseModelId;

    private Integer brandId;

    private Integer status;

    private Integer carServiceId;

    private Integer saleId;

    private String remark;

    private Integer createId;

    private Integer updateId;

    private Integer cityId;

    private Integer orgId;

    private BigDecimal rent;

    private BigDecimal rentRatio;

    private Date createTime;

    private Date updateTime;

    private Integer replaceFlag;

    private Integer goodsId;

    private Integer contractCommodityId;

    // 续签合同迭代增加的字段-----------start
    private Long contractId;

    private String oaContractCode;

    private BigDecimal deposit;

    private String leaseBeginDate;

    private Integer leaseMonths;

    private Integer leaseDays;

    private String leaseEndDate;

    private String billingAdjustmentDate;

    private Integer renewalType;

    private String expectRecoverDate;

    //增加实缴、应缴押金

    private BigDecimal payableDeposit;

    private BigDecimal paidInDeposit;

    @Resource
    private ServeGateway serveGateway;

    @Resource
    private ServeChangeRecordGateway serveChangeRecordGateway;

    @Resource
    private MqTools mqTools;

    @Value("${rocketmq.listenEventTopic}")

    @Transient
    private String event;

    @Resource
    private ServeAdjustRecordGateway serveAdjustRecordGateway;

    @Resource
    private BeanFactory beanFactory;

    @Override
    public void reactiveServe(ReactivateServeCmd cmd) {
        // 保存操作记录
        ServeEntity rawServe = serveGateway.getServeByServeNo(cmd.getServeNo());

        ServeEntity newServe = new ServeEntity();
        newServe.setStatus(ServeEnum.NOT_PRESELECTED.getCode());
        serveGateway.updateServeByServeNo(cmd.getServeNo(), newServe);
        saveChangeRecordWithReactive(cmd, rawServe, newServe);
    }

    // 保存重新激活类型的操作记录
    private void saveChangeRecordWithReactive(ReactivateServeCmd cmd, ServeEntity rawServe, ServeEntity newServe) {
        ServeChangeRecordPO serveChangeRecordPO = new ServeChangeRecordPO();
        serveChangeRecordPO.setServeNo(rawServe.getServeNo());
        serveChangeRecordPO.setType(ServeChangeRecordEnum.REACTIVE.getCode());
        serveChangeRecordPO.setRenewalType(0);
        serveChangeRecordPO.setRawData(JSONUtil.toJsonStr(rawServe));
        serveChangeRecordPO.setNewData(JSONUtil.toJsonStr(newServe));
        serveChangeRecordPO.setDeliverNo(cmd.getDeliverNo());
        serveChangeRecordPO.setReactiveReason(cmd.getReason());
        serveChangeRecordPO.setRemark(cmd.getRemark());
        serveChangeRecordPO.setCreatorId(cmd.getOperatorId());
        serveChangeRecordGateway.insert(serveChangeRecordPO);
    }

    @Override
    public ServeDepositDTO getServeDepositByServeNo(CustomerDepositListDTO customerDepositLisDTO) {
        ServeEntity serveEntity = serveGateway.getServeDepositByServeNo(customerDepositLisDTO);
        if (Objects.isNull(serveEntity)) {
            return null;
        }
        ServeDepositDTO serveDepositDTO = BeanUtil.copyProperties(serveEntity, ServeDepositDTO.class);
        //维修费用确认情况
        if (serveEntity.getStatus() < ServeEnum.DELIVER.getCode()) {
            serveDepositDTO.setMaintainFeeConfirmFlag(null);
        } else {
            serveDepositDTO.setMaintainFeeConfirmFlag(!serveEntity.getStatus().equals(ServeEnum.REPAIR.getCode()));
        }
        return serveDepositDTO;
    }

    @Override
    public PagePagination<ServeDepositDTO> getServeDepositByQry(CustomerDepositListDTO customerDepositLisDTO) {
        PagePagination<ServeEntity> servePagePagination = serveGateway.pageServeDeposit(customerDepositLisDTO);
        List<ServeEntity> serveList = servePagePagination.getList();
        List<ServeDepositDTO> depositDTOList = new ArrayList<>();
        for (ServeEntity serveEntity : serveList) {
            ServeDepositDTO serveDepositDTO = BeanUtil.copyProperties(serveEntity, ServeDepositDTO.class);
            //维修费用确认情况
            if (serveEntity.getStatus() < ServeEnum.DELIVER.getCode()) {
                serveDepositDTO.setMaintainFeeConfirmFlag(null);
            } else {
                serveDepositDTO.setMaintainFeeConfirmFlag(!serveEntity.getStatus().equals(ServeEnum.REPAIR.getCode()));
            }
            depositDTOList.add(serveDepositDTO);
        }
        PagePagination<ServeDepositDTO> depositPagePagination = new PagePagination<>();
        BeanUtil.copyProperties(servePagePagination, depositPagePagination);
        depositPagePagination.setList(depositDTOList);
        return depositPagePagination;
    }

    @Override
    public List<ServeDTO> getServeListByServeNoList(List<String> serveNoList) {
        List<ServeEntity> serveList = serveGateway.getServeByServeNoList(serveNoList);
        return CollectionUtil.isEmpty(serveList) ? CollectionUtil.newArrayList() : BeanUtil.copyToList(serveList, ServeDTO.class, new CopyOptions().ignoreError());
    }

    @Override
    public ServeDTO getServeByServeNo(String serveNo) {
        ServeEntity serve = serveGateway.getServeByServeNo(serveNo);
        return Objects.isNull(serve) ? null : BeanUtil.copyProperties(serve, ServeDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateServeDepositByServeNoList(Map<String, BigDecimal> updateDepositMap, Integer creatorId, Boolean isLockFlag) {
        List<String> serveNoList = new ArrayList<>(updateDepositMap.keySet());
        List<ServeEntity> serveList = serveGateway.getServeByServeNoList(serveNoList);
        List<ServeChangeRecordPO> recordList = new ArrayList<>();
        for (ServeEntity serveEntity : serveList) {
            ServeEntity updateDeposit = new ServeEntity();
            BigDecimal paidInDeposit = serveEntity.getPaidInDeposit();
            updateDeposit.setServeNo(serveEntity.getServeNo());
            updateDeposit.setPaidInDeposit(paidInDeposit.add(updateDepositMap.get(serveEntity.getServeNo())));
            updateDeposit.setStatus(isLockFlag ? serveEntity.getStatus() : ServeEnum.COMPLETED.getCode());
            serveGateway.updateServeByServeNo(serveEntity.getServeNo(), updateDeposit);

            if (updateDeposit.getStatus().equals(ServeEnum.COMPLETED.getCode())) {
                // 向合同域发送服务单已完成消息
                ServeDTO serveDTOToNoticeContract = new ServeDTO();
                serveDTOToNoticeContract.setServeNo(serveEntity.getServeNo());
                serveDTOToNoticeContract.setOaContractCode(serveEntity.getOaContractCode());
                // serveDTOToNoticeContract.setGoodsId(serveEntity.getContractCommodityId());
                serveDTOToNoticeContract.setCarServiceId(creatorId);
                serveDTOToNoticeContract.setRenewalType(serveEntity.getRenewalType());
                serveDTOToNoticeContract.setContractCommodityId(serveEntity.getContractCommodityId());
                mqTools.send(event, "recover_serve_to_contract", null, JSON.toJSONString(serveDTOToNoticeContract));
            }

            //变更记录
            ServeChangeRecordPO serveChangeRecord = new ServeChangeRecordPO();
            serveChangeRecord.setServeNo(serveEntity.getServeNo());
            String rawData = JSON.toJSONString(serveEntity);
            String newData = JSON.toJSONString(updateDeposit);
            serveChangeRecord.setRawData(rawData);
            serveChangeRecord.setNewData(newData);
            serveChangeRecord.setCreateTime(new Date());
            serveChangeRecord.setNewGoodsId(serveEntity.getGoodsId());
            serveChangeRecord.setRawGoodsId(serveEntity.getGoodsId());
            serveChangeRecord.setRenewalType(0);
            serveChangeRecord.setCreatorId(creatorId);
            serveChangeRecord.setNewBillingAdjustmentDate("");
            serveChangeRecord.setDeliverNo("");
            serveChangeRecord.setReactiveReason(0);
            serveChangeRecord.setRemark("");
            serveChangeRecord.setType(updateDeposit.getStatus().equals(ServeEnum.COMPLETED.getCode()) ?
                    ServeChangeRecordEnum.DEPOSIT_UNLOCK.getCode() : ServeChangeRecordEnum.DEPOSIT_LOCK.getCode());
            recordList.add(serveChangeRecord);
        }
        serveChangeRecordGateway.insertList(recordList);
    }

    @Override
    @Transactional
    public void serveAdjustment(ServeAdjustCmd cmd) {

        ServeEntity rawEntity = serveGateway.getServeByServeNo(cmd.getServeNo());

        ServeEntity newEntity = new ServeEntity();
        newEntity.setLeaseModelId(ServeAdjustChargeRentTypeEnum.NORMAL.getCode());
        newEntity.setExpectRecoverDate(FormatUtil.ymdFormatDateToString(cmd.getExpectRecoverTime()));
        newEntity.setDeposit(cmd.getChargeDepositAmount());
        newEntity.setReplaceFlag(JudgeEnum.NO.getCode());
        newEntity.setUpdateId(cmd.getOperatorId());
        newEntity.setUpdateTime(new Date());
        log.info("newEntity---->{}", newEntity);
        serveGateway.updateServeByServeNo(cmd.getServeNo(), newEntity);

        saveServeAdjustRecord(cmd);

        saveChangeRecord(rawEntity, newEntity, ServeChangeRecordEnum.REPLACE_ADJUST.getCode(), "", 0, "", cmd.getOperatorId());
    }

    @Override
    public void cancelServe(ServeCancelCmd cmd) {

        ServeEntity rawEntity = serveGateway.getServeByServeNo(cmd.getServeNo());

        ServeEntity newEntity = new ServeEntity();
        newEntity.setStatus(ServeEnum.CANCEL.getCode());
        newEntity.setUpdateId(cmd.getOperatorId());
        newEntity.setUpdateTime(new Date());
        serveGateway.updateServeByServeNo(cmd.getServeNo(), newEntity);

        saveChangeRecord(rawEntity, newEntity, ServeChangeRecordEnum.CANCEL.getCode(), "", 0, "", cmd.getOperatorId());
    }

    @Override
    @Transactional
    public void saveServeAdjustRecord(ServeAdjustCmd cmd) {

        ServeAdjustRecordPO po = serveAdjustRecordGateway.getRecordByServeNo(cmd.getServeNo());
        po = Optional.ofNullable(po).isPresent() ? po : new ServeAdjustRecordPO();
        po.setServeNo(cmd.getServeNo());
        po.setChargeLeaseModelId(ServeAdjustChargeRentTypeEnum.NORMAL.getCode());
        po.setChargeRentAmount(cmd.getChargeRentAmount());
        po.setChargeDepositAmount(cmd.getChargeDepositAmount());
        po.setExpectRecoverTime(cmd.getExpectRecoverTime());
        po.setDepositPayType(cmd.getDepositPayType());
        log.info("ServeAdjustRecordPO---->{}", po);
        if (!Optional.ofNullable(po).map(ServeAdjustRecordPO::getId).isPresent()) {
            po.setCreateId(cmd.getOperatorId());
            po.setCreateTime(new Date());
            serveAdjustRecordGateway.saveRecord(po);
        } else {
            serveAdjustRecordGateway.updateRecord(po);
        }
    }

    private void saveChangeRecord(ServeEntity rawServe, ServeEntity newServe, Integer type, String deliverNo, Integer reason, String remark, Integer createId) {

        ServeChangeRecordPO serveChangeRecordPO = new ServeChangeRecordPO();
        serveChangeRecordPO.setServeNo(rawServe.getServeNo());
        serveChangeRecordPO.setType(type);
        serveChangeRecordPO.setRenewalType(0);
        serveChangeRecordPO.setRawData(JSONUtil.toJsonStr(rawServe));
        serveChangeRecordPO.setNewData(JSONUtil.toJsonStr(newServe));
        serveChangeRecordPO.setDeliverNo(deliverNo);
        serveChangeRecordPO.setReactiveReason(reason);
        serveChangeRecordPO.setRemark(remark);
        serveChangeRecordPO.setCreatorId(createId);
        log.info("serveChangeRecordPO---->{}", serveChangeRecordPO);
        serveChangeRecordGateway.insert(serveChangeRecordPO);
    }
}
