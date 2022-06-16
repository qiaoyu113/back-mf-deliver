package com.mfexpress.rent.deliver.dto.data.serve.cmd;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "服务单调整命令")
public class ServeAdjustCmd extends BaseCmd {

    @ApiModelProperty(value = "服务单编号", required = true)
    private String serveNo;

    /**
     * 变更后租赁方式：1、正常租赁
     */
    @ApiModelProperty(value = "变更后租赁方式：1、正常租赁", required = false)
    private Integer chargeRentType;

    /**
     * 变更后租金
     */
    @ApiModelProperty(value = "变更后租金", required = false)
    private BigDecimal chargeRentAmount;

    /**
     * 变更后押金
     */
    @ApiModelProperty(value = "变更后押金", required = false)
    private BigDecimal chargeDepositAmount;

    /**
     * 实缴押金金额
     */
    @ApiModelProperty(value = "实缴押金金额")
    private BigDecimal paidInDepositAmount;

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
