package com.mfexpress.rent.deliver.dto.data.serve;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("替换车生成服务单")
public class ServeReplaceVehicleAddDTO {

    @ApiModelProperty(value = "原服务单编号")
    private String serveNo;

    @ApiModelProperty(value = "替换车品牌id")
    private Integer brandId;

    @ApiModelProperty(value = "替换车车型id")
    private Integer modelsId;

    @ApiModelProperty(value = "月租金")
    private BigDecimal rent;

    @ApiModelProperty(value = "租金比例")
    private BigDecimal rentRatio;

    @ApiModelProperty(value = "创建人id")
    private Integer creatorId;

}
