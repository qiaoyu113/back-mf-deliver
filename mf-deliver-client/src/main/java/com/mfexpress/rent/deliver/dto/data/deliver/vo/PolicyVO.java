package com.mfexpress.rent.deliver.dto.data.deliver.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value = "保单VO")
public class PolicyVO {

    @ApiModelProperty(value = "保单ID")
    private String policyId;

    @ApiModelProperty(value = "保单号")
    private String policyNo;

    @ApiModelProperty(value = "投保公司/人")
    private String policyHolder;

    @ApiModelProperty(value = "承保公司名称")
    private String insuranceCompanyName;

    @ApiModelProperty(value = "起保时间")
    private Date startInsureDate;

    @ApiModelProperty(value = "终保时间")
    private Date endInsureDate;

}
