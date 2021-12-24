package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel(value = "人车合照VO")
@Data
public class GroupPhotoVO {

    @ApiModelProperty(value = "车牌号")
    private String carNum;

    @ApiModelProperty(value = "图片链接")
    private String imgUrl;

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;

    @ApiModelProperty(value = "车辆id")
    private Integer carId;

    @ApiModelProperty(value = "交付单编号")
    private String deliverNo;

}
