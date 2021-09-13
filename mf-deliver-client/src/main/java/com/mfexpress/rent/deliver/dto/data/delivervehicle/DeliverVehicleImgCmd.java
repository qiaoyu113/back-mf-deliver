package com.mfexpress.rent.deliver.dto.data.delivervehicle;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("合照信息")
public class DeliverVehicleImgCmd {

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;
    @ApiModelProperty(value = "车辆id")
    private Integer carId;
    @ApiModelProperty(value = "交付单编号")
    private String deliverNo;
    @ApiModelProperty(value = "imgUrl")
    private String imgUrl;
}
