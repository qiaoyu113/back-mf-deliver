package com.mfexpress.rent.deliver.dto.data.deliver.cmd;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "取消预选命令")
public class CancelPreSelectedCmd extends BaseCmd {

    @ApiModelProperty(value = "车辆id", required = true)
    @NotNull(message = "车辆id不能为空")
    private Integer vehicleId;

    @ApiModelProperty(value = "二次操作标志位，1：真，0：假")
    @NotNull(message = "操作标志位不能为空")
    private Integer secondOperationFlag;

    @ApiModelProperty(value = "服务单号")
    private String serveNo;

    @ApiModelProperty(value = "交付单号")
    private String deliverNo;

}
