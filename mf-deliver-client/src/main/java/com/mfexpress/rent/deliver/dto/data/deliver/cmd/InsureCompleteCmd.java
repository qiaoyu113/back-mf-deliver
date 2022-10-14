package com.mfexpress.rent.deliver.dto.data.deliver.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "投保完成命令")
public class InsureCompleteCmd {

    @ApiModelProperty(value = "交付单号", required = true)
    @NotBlank(message = "交付单号不能为空")
    private String deliverNo;

    @ApiModelProperty(value = "交强险保单id")
    private String compulsoryInsurancePolicyId;

    @ApiModelProperty(value = "交强险保单号")
    private String compulsoryInsurancePolicyNo;

    @ApiModelProperty(value = "商业险保单id")
    private String commercialInsurancePolicyId;

    @ApiModelProperty(value = "商业险保单号")
    private String commercialInsurancePolicyNo;

}
