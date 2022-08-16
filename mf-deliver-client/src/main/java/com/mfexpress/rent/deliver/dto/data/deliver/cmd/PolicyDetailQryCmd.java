package com.mfexpress.rent.deliver.dto.data.deliver.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "保单查询")
public class PolicyDetailQryCmd {

    @ApiModelProperty(value = "保单ID")
    private String policyId;

}
