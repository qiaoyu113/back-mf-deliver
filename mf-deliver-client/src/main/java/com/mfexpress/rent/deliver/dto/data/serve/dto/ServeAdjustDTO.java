package com.mfexpress.rent.deliver.dto.data.serve.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServeAdjustDTO {

    /**
     * 服务单号
     */
    private String serveNo;

    /**
     * 原车服务单号
     */
    private String sourceServeNo;

    /**
     * 变更后租赁方式
     */
    private Integer chargeLeaseModelId;

    /**
     * 变更后租金金额
     */
    private BigDecimal chargeRentAmount;

    /**
     * 调整后租金比例
     */
    private BigDecimal chargeRentRatio;

    /**
     * 变更后押金金额
     */
    private BigDecimal chargePayableDepositAmount;

    /**
     * 变更后实缴押金金额
     */
    private BigDecimal chargePaidInDepositAmount;

    /**
     * 预计收车日期
     */
    private Date expectRecoverTime;

    /**
     * 押金支付方式：1、押金账本支付;2、原车押金
     */
    private Integer depositPayType;

    /**
     * 调整状态:1-待调整；2-已调整计费；3-已完成；
     */
    private Integer adjustStatus;

    /**
     * 调整人
     */
    private Integer adjustId;

    /**
     * 调整时间
     */
    private Date adjustTime;
}
