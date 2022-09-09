package com.mfexpress.rent.deliver.dto.data.deliver.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "申请查询返回结果")
public class InsuranceApplyRentVO {

    @ApiModelProperty(value = "申请ID")
    private String applyId;

    @ApiModelProperty(value = "申请编号")
    private String applyCode;

    @ApiModelProperty(value = "车辆Id")
    private String vehicleId;

    @ApiModelProperty(value = "申请状态")
    private Integer applyStatus;

    @ApiModelProperty(value = "申请状态含义")
    private String applyStatusName;

    @ApiModelProperty(value = "保单编号")
    private String policyNo;

}
