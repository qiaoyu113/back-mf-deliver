package com.mfexpress.rent.deliver.entity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.starter.tools.mq.MqTools;
import com.mfexpress.rent.deliver.constant.ServeChangeRecordEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.CancelPreSelectedCmd;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositListDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDepositDTO;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeCancelCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServePaidInDepositUpdateCmd;
import com.mfexpress.rent.deliver.entity.api.ServeEntityApi;
import com.mfexpress.rent.deliver.gateway.ServeChangeRecordGateway;
import com.mfexpress.rent.deliver.gateway.ServeGateway;
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
    public void updateServeDepositByServeNoList(Map<String, BigDecimal> updateDepositMap, Integer creatorId, Boolean isLockFlag, Boolean isTermination) {
        List<String> serveNoList = new ArrayList<>(updateDepositMap.keySet());
        List<ServeEntity> serveList = serveGateway.getServeByServeNoList(serveNoList);
        List<ServeChangeRecordPO> recordList = new ArrayList<>();
        for (ServeEntity serveEntity : serveList) {
            ServeEntity updateDeposit = new ServeEntity();
            BigDecimal paidInDeposit = serveEntity.getPaidInDeposit();
            updateDeposit.setServeNo(serveEntity.getServeNo());
            updateDeposit.setPaidInDeposit(paidInDeposit.add(updateDepositMap.get(serveEntity.getServeNo())));
            if (!isTermination) {
                updateDeposit.setStatus(isLockFlag ? serveEntity.getStatus() : ServeEnum.COMPLETED.getCode());
            }
            serveGateway.updateServeByServeNo(serveEntity.getServeNo(), updateDeposit);

            if (!isTermination) {
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
            if (!isTermination) {
                serveChangeRecord.setType(updateDeposit.getStatus().equals(ServeEnum.COMPLETED.getCode()) ?
                        ServeChangeRecordEnum.DEPOSIT_UNLOCK.getCode() : ServeChangeRecordEnum.DEPOSIT_LOCK.getCode());
            }else {
                serveChangeRecord.setType(ServeChangeRecordEnum.DEPOSIT_UNLOCK.getCode());
            }

            recordList.add(serveChangeRecord);
        }
        serveChangeRecordGateway.insertList(recordList);
    }

    @Override
    @Transactional
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
    public void saveChangeRecord(ServeEntity rawServe, ServeEntity newServe, Integer type, String deliverNo, Integer reason, String remark, Integer createId) {

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

    @Override
    @Transactional
    public Integer updateServePaidInDeposit(ServePaidInDepositUpdateCmd cmd) {

        ServeEntity serve = serveGateway.getServeByServeNo(cmd.getServeNo());

        ServeEntity updateServe = new ServeEntity();
        updateServe.setPaidInDeposit(serve.getPaidInDeposit().add(cmd.getChargeDepositAmount()));

        return serveGateway.updateServeByServeNo(cmd.getServeNo(), updateServe);
    }

    @Override
    public Integer cancelSelected(CancelPreSelectedCmd cmd) {
        ServeEntity serveEntity = serveGateway.getServeByServeNo(cmd.getServeNo());
        if (null == serveEntity) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "服务单查询失败");
        }

        ServeEntity serveEntityToUpdate = ServeEntity.builder().status(ServeEnum.NOT_PRESELECTED.getCode()).build();
        serveGateway.updateServeByServeNo(serveEntity.getServeNo(), serveEntityToUpdate);
        return 0;
    }

    @Override
    public Boolean terminationServe(ServeDTO serveDTO) {
        ServeEntity serveEntity = BeanUtil.toBean(serveDTO, ServeEntity.class);
        serveEntity.setStatus(ServeEnum.CANCEL.getCode());
        serveGateway.updateServe(serveEntity);

        ServeChangeRecordPO serveChangeRecordPO = new ServeChangeRecordPO();
        serveChangeRecordPO.setServeNo(serveDTO.getServeNo());
        serveChangeRecordPO.setType(ServeChangeRecordEnum.TERMINATION.getCode());
        serveChangeRecordPO.setCreatorId(serveDTO.getCreateId());
        serveChangeRecordPO.setRenewalType(0);
        serveChangeRecordGateway.insert(serveChangeRecordPO);

        return Boolean.TRUE;
    }

    @Override
    public List<ServeDTO> getServeDTOByCustomerId(Integer customerId) {
        List<ServeEntity> serve = serveGateway.getServeByCustomerId(customerId);
        if (CollectionUtil.isEmpty(serve)){
            return new ArrayList<>();
        }
        return BeanUtil.copyToList(serve, ServeDTO.class, CopyOptions.create().ignoreError());
    }

    /*@Override
    public Integer updateServePayableDeposit(ServeUpdatePayableDepositCmd cmd) {
        ServeEntity serveEntity = new ServeEntity();
        serveEntity.setServeNo(cmd.getServeNo());
        serveEntity.setDeposit(cmd.getDepositAmount());
        serveEntity.setPayableDeposit(cmd.getDepositAmount());
        return serveGateway.updateServePayableDepositByContractCommodityId(serveEntity);
    }*/
}
