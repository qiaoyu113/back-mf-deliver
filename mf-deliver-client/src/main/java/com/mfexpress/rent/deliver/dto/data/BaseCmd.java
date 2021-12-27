package com.mfexpress.rent.deliver.dto.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BaseCmd {

    @ApiModelProperty(value = "操作人id")
    private Integer operatorId;

    @ApiModelProperty(value = "操作人姓名")
    private String operatorName;

}