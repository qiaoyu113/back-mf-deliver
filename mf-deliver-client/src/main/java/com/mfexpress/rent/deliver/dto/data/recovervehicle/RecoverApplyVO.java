package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("申请收车页选择车辆信息")
public class RecoverApplyVO {

    @ApiModelProperty(value = "租赁服务单编号")
    private String serveNo;
    @ApiModelProperty(value = "车牌号")
    private String carNum;
    @ApiModelProperty(value = "交付单编号")
    private String deliverNo;
    @ApiModelProperty(value = "车辆id")
    private Integer carId;


}
