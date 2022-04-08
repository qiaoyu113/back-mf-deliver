package com.mfexpress.rent.deliver.dto.data.recovervehicle;


import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel("取消收车")
public class RecoverCancelCmd extends BaseCmd {

    @ApiModelProperty("服务单编号")
    private String serveNo;

    @ApiModelProperty(value = "取消收车原因")
    private Integer cancelRemarkId;

    @ApiModelProperty(value = "取消收车原因id")
    private String cancelRemark;

    private Integer carServiceId;
}

