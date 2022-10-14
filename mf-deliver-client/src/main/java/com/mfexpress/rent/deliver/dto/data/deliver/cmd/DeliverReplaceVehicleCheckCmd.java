package com.mfexpress.rent.deliver.dto.data.deliver.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "发车更新车辆操作检查相关条件是否符合")
public class DeliverReplaceVehicleCheckCmd {

    @ApiModelProperty(value = "服务单号", required = true)
    @NotBlank(message = "服务单号不能为空")
    private String serveNo;

    @ApiModelProperty(value = "交付单号")
    private String deliverNo;

}
