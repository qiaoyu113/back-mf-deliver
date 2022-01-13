package com.mfexpress.rent.deliver.dto.data.recovervehicle;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@ApiModel("违章处理")
@Data
public class RecoverDeductionCmd {

    @ApiModelProperty(value = "租赁服务单编号")
    @NotEmpty(message = "租赁服务单编号不能为空")
    private String serveNo;

    @ApiModelProperty(value = "退保处理方式", example = "1:车辆无违章，2:客户自行处理，3:代客户处理")
    @NotNull(message = "退保处理方式不能为空")
    private Integer deductionHandel;

    @ApiModelProperty(value = "违章分数")
    private Integer violationPoints;
    @ApiModelProperty(value = "消分金额")
    private BigDecimal deductionAmount;
    @ApiModelProperty(value = "代办金额")
    private BigDecimal agencyAmount;
    private Integer carServiceId;

    @ApiModelProperty(value = "车损费")
    @NotNull(message = "退保处理方式不能为空")
    private Double damageFee;

    @ApiModelProperty(value = "路边停车费")
    private Double parkFee;

}
