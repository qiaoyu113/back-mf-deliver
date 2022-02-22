package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel(value = "交付单图片信息(人车合照)")
@Data
public class DeliverImgInfo {

    @ApiModelProperty(value = "服务单编号", required = true)
    @NotEmpty(message = "交付单图片信息中服务单编号")
    private String serveNo;

    @ApiModelProperty(value = "车辆id", required = true)
    @NotNull(message = "车辆id不能为空")
    private Integer carId;

    @ApiModelProperty(value = "交付单编号", required = true)
    @NotEmpty(message = "交付单编号不能为空")
    private String deliverNo;

    @ApiModelProperty(value = "imgUrl", required = true)
    @NotEmpty(message = "人车合照图片链接不能为空")
    private String imgUrl;

    @ApiModelProperty(value = "车牌号", required = true)
    @NotEmpty(message = "车牌号不能为空")
    private String carNum;

}

