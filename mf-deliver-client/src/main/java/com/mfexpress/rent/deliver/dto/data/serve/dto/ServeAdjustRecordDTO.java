package com.mfexpress.rent.deliver.dto.data.serve.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Deprecated
@Data
@ApiModel(value = "替换单调整记录 DTO")
public class ServeAdjustRecordDTO {

    @ApiModelProperty(value = "服务单编号", required = true)
    private String serveNo;

    /**
     * 变更后租赁方式：1、正常租赁
     */
    @ApiModelProperty(value = "变更后租赁方式：1、正常租赁", required = true)
    private Integer chargeLeaseModelId;

    /**
     * 变更后租金
     */
    @ApiModelProperty(value = "变更后租金", required = true)
    private BigDecimal chargeRentAmount;

    /**
     * 变更后押金
     */
    @ApiModelProperty(value = "变更后押金", required = true)
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
    @ApiModelProperty(value = "押金支付方式：1、押金账本支付;2、原车押金", required = true)
    private Integer depositPayType;
}
