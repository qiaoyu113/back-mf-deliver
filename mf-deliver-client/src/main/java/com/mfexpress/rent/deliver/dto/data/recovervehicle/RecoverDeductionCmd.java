package com.mfexpress.rent.deliver.dto.data.recovervehicle;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@ApiModel("违章处理")
@Data
public class RecoverDeductionCmd {

    @ApiModelProperty(value = "租赁服务单编号")
    private String serveNo;
    @ApiModelProperty(value = "退保处理方式", example = "1:车辆无违章，2:客户自行处理，3:待客户处理")
    private Integer deductionHandel;
    @ApiModelProperty(value = "违章分数")
    private Integer violationPoints;
    @ApiModelProperty(value = "保险金额")
    private BigDecimal deductionAmount;
    @ApiModelProperty(value = "代办金额")
    private BigDecimal agencyAmount;
    private Integer carServiceId;

}
