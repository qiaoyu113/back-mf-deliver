package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("通过交付单查询服务单命令")
public class ServeQryByDeliverCmd {

    @ApiModelProperty(value = "交付单编号", required = true)
    @NotEmpty(message = "交付单编号不能为空")
    private String deliverNo;
}
