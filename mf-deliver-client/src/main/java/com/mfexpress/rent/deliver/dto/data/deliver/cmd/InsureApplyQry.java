package com.mfexpress.rent.deliver.dto.data.deliver.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "通过服务单查询其对应的保险信息")
public class InsureApplyQry {

    @ApiModelProperty(value = "交付单号", required = true)
    @NotBlank(message = "交付单号不能为空")
    private String deliverNo;

    @ApiModelProperty(value = "保险申请类型")
    private Integer type;

}
