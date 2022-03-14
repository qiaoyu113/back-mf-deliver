package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "通过交付单取消收车命令")
public class RecoverCancelByDeliverCmd extends BaseCmd {

    @ApiModelProperty(value = "交付单编号", required = true)
    @NotEmpty(message = "交付单编号不能为空")
    private String deliverNo;

    @ApiModelProperty(value = "取消收车原因")
    private Integer cancelRemarkId;

    @ApiModelProperty(value = "取消收车原因id")
    private String cancelRemark;
}
