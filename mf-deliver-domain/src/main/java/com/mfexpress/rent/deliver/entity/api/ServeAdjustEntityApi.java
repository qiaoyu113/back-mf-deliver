package com.mfexpress.rent.deliver.entity.api;

import com.mfexpress.rent.deliver.constant.AdjustStatusEnum;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCompletedCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustStartBillingCmd;
import com.mfexpress.rent.deliver.entity.ServeEntity;
import com.mfexpress.rent.deliver.po.ServeAdjustOperatorRecordPO;
import com.mfexpress.rent.deliver.po.ServeAdjustPO;

import java.util.Date;

public interface ServeAdjustEntityApi {

    /**
     * 保存调整工单
     *
     * @return
     */
    int save(ServeAdjustCmd cmd);

    /**
     * 调整工单开始计费
     *
     * @return
     */
    int startBilling(ServeAdjustStartBillingCmd cmd);

    /**
     * 完成调整工单
     *
     * @return
     */
    int completed(ServeAdjustCompletedCmd cmd);


    default ServeAdjustPO initSave(ServeAdjustCmd cmd) {

        return ServeAdjustPO.builder().serveNo(cmd.getServeNo())
                .sourceServeNo(cmd.getSourceServeNo())
                .chargeLeaseModelId(cmd.getChargeLeaseModelId())
                .chargeRentAmount(cmd.getChargeRentAmount())
                .chargeRentRatio(cmd.getChargeRentRatio())
                .chargePayableDepositAmount(cmd.getChargePayableDepositAmount())
                .chargePaidInDepositAmount(cmd.getChargePaidInDepositAmount())
                .expectRecoverTime(cmd.getExpectRecoverTime())
                .depositPayType(cmd.getDepositPayType())
                .adjustStatus(AdjustStatusEnum.NOT_ADJUST.getIndex())
                .adjustId(cmd.getOperatorId())
                .adjustTime(new Date()).build();
    }

    default ServeAdjustOperatorRecordPO initOperatorRecord(ServeEntity entity, ServeAdjustPO po, Integer operatorId, Date startBillingDate) {

        return ServeAdjustOperatorRecordPO.builder()
                .serveNo(entity.getServeNo())
                .leaseModelId(entity.getLeaseModelId())
                .rentAmount(po.getChargeRentAmount())
                .rentRatio(entity.getRentRatio())
                .payableDepositAmount(po.getChargePayableDepositAmount())
                .paidInDepositAmount(entity.getPaidInDeposit())
                .depositPayType(po.getDepositPayType())
                .startBillingDate(startBillingDate)
                .operatorType(po.getAdjustStatus())
                .operatorId(operatorId)
                .operatorTime(new Date()).build();
    }

}
