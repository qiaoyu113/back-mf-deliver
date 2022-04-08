package com.mfexpress.rent.deliver.dto.data.deliver;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class DeliverCheckCmd {

    @ApiModelProperty(value = "服务单编号", required = true)
    @NotEmpty(message = "服务单编号不能为空")
    private String serveNo;

    private Integer carServiceId;
}
