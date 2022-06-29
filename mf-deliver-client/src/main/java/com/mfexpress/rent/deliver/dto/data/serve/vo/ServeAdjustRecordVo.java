package com.mfexpress.rent.deliver.dto.data.serve.vo;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Deprecated
@Slf4j
@Data
@ApiModel(value = "服务单调整记录VO")
public class ServeAdjustRecordVo {

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;

    /**
     * 变更后租赁方式：1、正常租赁
     */
    @ApiModelProperty(value = "变更后租赁方式：1、正常租赁")
    private Integer chargeLeaseModelId;

    @ApiModelProperty(value = "变更后租赁方式Label")
    private String chargeLeaseModel;

    /**
     * 变更后租金/应缴押金金额
     */
    @ApiModelProperty(value = "变更后租金")
    private BigDecimal chargeRentAmount;

    /**
     * 变更后的租金比例
     */
    @ApiModelProperty(value = "变更后的租金比例")
    private BigDecimal chargeRentRatio;

    /**
     * 实缴押金金额
     */
    @ApiModelProperty(value = "实缴押金金额")
    private BigDecimal paidInDepositAmount;

    /**
     * 变更后押金
     */
    @ApiModelProperty(value = "变更后押金")
    private BigDecimal chargeDepositAmount;

    /**
     * 预计收车日期
     */
    @ApiModelProperty(value = "预计收车日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expectRecoverTime;

    /**
     * 押金支付方式：1、押金账本支付;2、原车押金
     */
    @ApiModelProperty(value = "押金支付方式：1、押金账本支付;2、原车押金")
    private Integer depositPayType;

    @ApiModelProperty(value = "原车辆ID")
    private Integer sourceCarId;

    @ApiModelProperty(value = "原车牌号")
    private String sourcePlate;

    @ApiModelProperty(value = "未锁定押金账本金额")
    private BigDecimal unlockDepositAmount;

    @ApiModelProperty("客户id")
    private  Integer customerId;

    @ApiModelProperty("订单id")
    private  Long orderId;

}
