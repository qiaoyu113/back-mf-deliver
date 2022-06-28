package com.mfexpress.rent.deliver.entity;

import com.mfexpress.rent.deliver.constant.AdjustStatusEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ServeChangeRecordEnum;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCompletedCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustStartBillingCmd;
import com.mfexpress.rent.deliver.entity.api.ServeAdjustEntityApi;
import com.mfexpress.rent.deliver.entity.api.ServeEntityApi;
import com.mfexpress.rent.deliver.gateway.DeliverGateway;
import com.mfexpress.rent.deliver.gateway.ServeAdjustGateway;
import com.mfexpress.rent.deliver.gateway.ServeAdjustOperatorRecordGateway;
import com.mfexpress.rent.deliver.gateway.ServeGateway;
import com.mfexpress.rent.deliver.po.ServeAdjustOperatorRecordPO;
import com.mfexpress.rent.deliver.po.ServeAdjustPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Component
public class ServeAdjustEntity implements ServeAdjustEntityApi {

    @Resource
    ServeAdjustGateway serveAdjustGateway;

    @Resource
    ServeGateway serveGateway;

    @Resource
    ServeAdjustOperatorRecordGateway recordGateway;

    @Resource
    ServeEntityApi serveEntityApi;

    @Resource
    DeliverGateway deliverGateway;

    @Override
    @Transactional
    public int save(ServeAdjustCmd cmd) {

        ServeEntity rawEntity = serveGateway.getServeByServeNo(cmd.getServeNo());

        ServeAdjustPO po = initSave(cmd);
        po.setChargePaidInDepositAmount(rawEntity.getPaidInDeposit());

        // 调整工单保存
        serveAdjustGateway.save(po);
        ServeAdjustOperatorRecordPO recordPO = initOperatorRecord(rawEntity, po, cmd.getOperatorId(), null);

        // 操作记录保存
        recordGateway.save(recordPO);

        return 1;
    }

    @Override
    @Transactional
    public int startBilling(ServeAdjustStartBillingCmd cmd) {

        ServeEntity rawEntity = serveGateway.getServeByServeNo(cmd.getServeNo());

        ServeAdjustPO po = serveAdjustGateway.getByServeNo(cmd.getServeNo());
        po.setAdjustStatus(AdjustStatusEnum.STRING_BILLING.getIndex());
        po.setChargePaidInDepositAmount(rawEntity.getPaidInDeposit());

        serveAdjustGateway.updateByServeNo(po);

        ServeAdjustOperatorRecordPO recordPO = initOperatorRecord(rawEntity, po, cmd.getOperatorId(), cmd.getStartBillingDate());

        // 操作记录保存
        recordGateway.save(recordPO);

        // 更新替换服务单
        ServeEntity newEntity = new ServeEntity();
        newEntity.setLeaseModelId(po.getChargeLeaseModelId());
        newEntity.setRent(po.getChargeRentAmount());
        newEntity.setRentRatio(po.getChargeRentRatio());
        newEntity.setDeposit(po.getChargePayableDepositAmount());
        newEntity.setPayableDeposit(po.getChargePayableDepositAmount());
        newEntity.setReplaceFlag(JudgeEnum.NO.getCode());
        newEntity.setUpdateId(cmd.getOperatorId());
        newEntity.setUpdateTime(new Date());

        serveGateway.updateServeByServeNo(rawEntity.getServeNo(), newEntity);

        // 保存服务单更改记录
        serveEntityApi.saveChangeRecord(rawEntity, newEntity, ServeChangeRecordEnum.REPLACE_ADJUST.getCode(), cmd.getDeliverNo(), 0, "替换掉开始计费", cmd.getOperatorId());

        return 1;
    }

    @Override
    @Transactional
    public int completed(ServeAdjustCompletedCmd cmd) {

        ServeEntity rawEntity = serveGateway.getServeByServeNo(cmd.getServeNo());

        ServeAdjustPO po = serveAdjustGateway.getByServeNo(cmd.getServeNo());
        po.setAdjustStatus(AdjustStatusEnum.COMPLETED.getIndex());
        po.setChargePaidInDepositAmount(rawEntity.getPaidInDeposit());

        serveAdjustGateway.updateByServeNo(po);

        po = serveAdjustGateway.getByServeNo(po.getServeNo());
        ServeAdjustOperatorRecordPO recordPO = initOperatorRecord(rawEntity, po, cmd.getOperatorId(), cmd.getStartBillingDate());
        recordPO.setPaidInDepositAmount(po.getChargePaidInDepositAmount());
        // 操作记录保存
        recordGateway.save(recordPO);

        return 1;
    }

}
