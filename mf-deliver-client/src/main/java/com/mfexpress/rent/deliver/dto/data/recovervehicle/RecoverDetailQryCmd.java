package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel(value = "收车申请详情查询命令")
public class RecoverDetailQryCmd {

    @NotEmpty(message = "服务单编号不能为空")
    @ApiModelProperty(value = "服务单编号")
    private String serveNo;

}
