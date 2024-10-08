package com.mfexpress.rent.deliver.dto.data.serve.cmd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(value = "服务单调整命令")
public class ServeAdjustCmd extends BaseCmd {

    @ApiModelProperty(value = "服务单编号", required = true)
    private String serveNo;

    @ApiModelProperty(value = "原车服务单号")
    private String sourceServeNo;

    /**
     * 变更后租赁方式：1、正常租赁
     */
    @ApiModelProperty(value = "变更后租赁方式：1、正常租赁", required = false)
    private Integer chargeLeaseModelId;

    /**
     * 变更后租金
     */
    @ApiModelProperty(value = "变更后租金", required = false)
    private BigDecimal chargeRentAmount;

    /**
     * 变更后的租金比例
     */
    @ApiModelProperty(value = "变更后的租金比例")
    private BigDecimal chargeRentRatio;

    @ApiModelProperty(value = "变更后押金", required = false)
    private BigDecimal chargeDepositAmount;

    /**
     * 变更后押金
     */
    @ApiModelProperty(value = "变更后应缴押金", required = false)
    private BigDecimal chargePayableDepositAmount;

    /**
     * 变更后实缴押金金额
     */
    @ApiModelProperty(value = "变更后实缴押金金额")
    private BigDecimal chargePaidInDepositAmount;

    /**
     * 预计收车日期
     */
    @ApiModelProperty(value = "预计收车日期", required = false)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expectRecoverTime;

    /**
     * 押金支付方式：1、押金账本支付;2、原车押金
     */
    @ApiModelProperty(value = "押金支付方式：1、押金账本支付;2、原车押金", required = true)
    private Integer depositPayType;

    @ApiModelProperty("客户id")
    private  Integer customerId;

    @ApiModelProperty("订单id")
    private  Long orderId;
}
