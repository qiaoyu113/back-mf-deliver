package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("电子交接单VO")
@Data
public class ElecHandoverDocVO {

    @ApiModelProperty(value = "电子交接单pdf文件url")
    private String fileUrl;

}
