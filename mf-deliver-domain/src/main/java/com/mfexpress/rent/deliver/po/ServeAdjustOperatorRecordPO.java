package com.mfexpress.rent.deliver.po;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.Table;

/**
 * 注释服务单调整操作记录
 *
 * @author hxcx
 * @date 2022-06-20 12:51
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "serve_adjust_operator_record")
public class ServeAdjustOperatorRecordPO {

    /**
     * 自增ID
     */
    private Integer id;

    /**
     * 服务单号
     */
    private String serveNo;

    /**
     * 当前租赁方式：1、正常租赁；2-试用；3-展示；4-优惠；5-替换
     */
    private Integer leaseModelId;

    /**
     * 当前租金金额
     */
    private BigDecimal rentAmount;

    /**
     * 当前租金比例
     */
    private BigDecimal rentRatio;

    /**
     * 应缴押金金额
     */
    private BigDecimal payableDepositAmount;

    /**
     * 当前实缴押金金额
     */
    private BigDecimal paidInDepositAmount;

    /**
     * 押金支付方式：1、押金账本支付;2、原车押金
     */
    private Integer depositPayType;

    /**
     * 开始计费日期
     */
    private Date startBillingDate;

    /**
     * 操作类型：1-保存；2-调整计费；3-完成
     */
    private Integer operatorType;

    /**
     * 操作人
     */
    private Integer operatorId;

    /**
     * 操作时间
     */
    private Date operatorTime;
}
