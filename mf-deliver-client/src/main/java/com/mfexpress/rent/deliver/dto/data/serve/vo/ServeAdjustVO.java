package com.mfexpress.rent.deliver.dto.data.serve.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "服务单调整工单前端交互VO")
public class ServeAdjustVO {

    /**
     * 服务单号
     */
    @ApiModelProperty(value = "服务单号")
    private String serveNo;

    /**
     * 原车服务单号
     */
    @ApiModelProperty(value = "原车服务单号")
    private String sourceServeNo;

    /**
     * 变更后租赁方式
     */
    @ApiModelProperty(value = "变更后租赁方式")
    private Integer chargeLeaseModelId;

    @ApiModelProperty(value = "变更后租赁方式Label")
    private String chargeLeaseModel;

    /**
     * 变更后租金金额
     */
    @ApiModelProperty(value = "变更后租金金额")
    private BigDecimal chargeRentAmount;

    /**
     * 调整后租金比例
     */
    @ApiModelProperty(value = "变更后租金比例")
    private BigDecimal chargeRentRatio;

    /**
     * 变更后应缴押金金额
     */
    @ApiModelProperty(value = "变更后应缴押金金额")
    private BigDecimal chargePayableDepositAmount;

    /**
     * 变更后实缴押金金额
     */
    @ApiModelProperty(value = "变更后实缴押金金额")
    private BigDecimal chargePaidInDepositAmount;

    /**
     * 预计收车日期
     */
    @ApiModelProperty(value = "预计收车日期")
    private Date expectRecoverTime;

    /**
     * 押金支付方式：1、押金账本支付;2、原车押金
     */
    @ApiModelProperty(value = "押金支付方式：1、押金账本支付;2、原车押金")
    private Integer depositPayType;

    /**
     * 原车辆ID
     */
    @ApiModelProperty(value = "原车辆ID")
    private Integer sourceCarId;

    /**
     * 原车牌号
     */
    @ApiModelProperty(value = "原车牌号")
    private String sourcePlate;

    /**
     * 未锁定押金账本金额
     */
    @ApiModelProperty(value = "未锁定押金账本金额")
    private BigDecimal unlockDepositAmount;

    /**
     * 客户id
     */
    @ApiModelProperty("客户id")
    private  Integer customerId;

    /**
     * 订单id
     */
    @ApiModelProperty("订单id")
    private  Long orderId;
}
