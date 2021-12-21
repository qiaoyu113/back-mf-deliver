package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "人车合照VO")
@Data
public class GroupPhotoVO {

    @ApiModelProperty(value = "车牌号")
    private String carNum;

    @ApiModelProperty(value = "图片链接")
    private String imgUrl;

}
