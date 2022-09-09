package com.mfexpress.rent.deliver.dto.data.deliver.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "提示信息vo")
@Data
public class TipVO {

    @ApiModelProperty(value = "提示信息标志位，1：有提示信息，0：无提示信息")
    private Integer tipFlag;

    @ApiModelProperty(value = "提示信息")
    private String tipMsg;

}
