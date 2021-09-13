package com.mfexpress.rent.deliver.dto.data.recovervehicle;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("收车申请信息")
public class RecoverApplyCmd {


    @ApiModelProperty(value = "服务单编号")
    private String serveNo;
    @ApiModelProperty(value = "交付单编号")
    private String deliverNo;
    @ApiModelProperty(value = "车辆id")
    private Integer carId;

}
