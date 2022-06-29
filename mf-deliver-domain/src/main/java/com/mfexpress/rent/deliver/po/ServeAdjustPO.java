package com.mfexpress.rent.deliver.po;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.Table;

/**
 * 注释服务单调整工单
 *
 * @author hxcx
 * @date 2022-06-20 12:52
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "serve_adjust")
public class ServeAdjustPO {

    /**
     * 自增ID
     */
    private Integer id;

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
