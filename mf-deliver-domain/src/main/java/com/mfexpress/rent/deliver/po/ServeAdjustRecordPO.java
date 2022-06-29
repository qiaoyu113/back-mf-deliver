package com.mfexpress.rent.deliver.po;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务单调整记录表
 */
@Deprecated
@Data
@Table(name = "serve_adjust_record")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServeAdjustRecordPO {

    @Id
    @Column(name = "id")
    private Integer id;

    /**
     * 服务单号
     */
    @Column(name = "serve_no")
    private String serveNo;

    /**
     * 变更后租赁方式：1、正常租赁
     */
    @Column(name = "charge_lease_model_id")
    private Integer chargeLeaseModelId;

    /**
     * 变更后租金
     */
    @Column(name = "charge_rent_amount")
    private BigDecimal chargeRentAmount;

    /**
     * 变更后租金
     */
    @Column(name = "charge_deposit_amount")
    private BigDecimal chargeDepositAmount;

    /**
     * 预计收车日期
     */
    @Column(name = "expect_recover_time")
    private Date expectRecoverTime;

    /**
     * 押金支付方式：1、押金账本支付;2、原车押金
     */
    @Column(name = "deposit_pay_type")
    private Integer depositPayType;

    /**
     * 创建人
     */
    @Column(name = "create_id")
    private Integer createId;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;
}
