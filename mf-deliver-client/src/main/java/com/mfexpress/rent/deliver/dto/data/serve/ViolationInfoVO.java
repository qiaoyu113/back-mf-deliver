package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ViolationInfoVO {

    @ApiModelProperty(value = "处理方式")
    private Integer deductionHandle;

    @ApiModelProperty(value = "违章分数")
    private Integer violationPoints;

    @ApiModelProperty(value = "消分总金额")
    private BigDecimal deductionAmount;

    @ApiModelProperty(value = "代办总金额")
    private BigDecimal agencyAmount;
}
