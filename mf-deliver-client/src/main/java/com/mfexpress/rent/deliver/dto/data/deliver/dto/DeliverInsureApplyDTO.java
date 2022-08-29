package com.mfexpress.rent.deliver.dto.data.deliver.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value = "后市场端返回的车辆投保申请DTO")
public class DeliverInsureApplyDTO {

    @ApiModelProperty(value = "交强险申请id")
    private String compulsoryApplyId;

    @ApiModelProperty(value = "交强险申请编号")
    private String compulsoryApplyCode;

    @ApiModelProperty(value = "商业险申请id")
    private String commercialApplyId;

    @ApiModelProperty(value = "商业险申请编号")
    private String commercialApplyCode;

    @ApiModelProperty(value = "车辆id")
    private Integer vehicleId;

    @ApiModelProperty(value = "交付单号")
    private String deliverNo;

    @ApiModelProperty(value = "交强险保单号")
    private String compulsoryInsurancePolicyNo;

    @ApiModelProperty(value = "商业险保单号")
    private String commercialInsurancePolicyNo;

    @ApiModelProperty(value = "申请时间")
    private Date applyTime;

    // private Integer

}
