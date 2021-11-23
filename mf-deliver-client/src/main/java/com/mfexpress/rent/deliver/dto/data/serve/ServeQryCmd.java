package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("服务单详情查询")
public class ServeQryCmd {

    @NotEmpty(message = "服务单编号不能为空")
    @ApiModelProperty(value = "服务单编号")
    private String serveNo;

}
