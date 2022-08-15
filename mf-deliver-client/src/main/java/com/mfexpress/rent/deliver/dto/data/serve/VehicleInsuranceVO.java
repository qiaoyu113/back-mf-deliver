package com.mfexpress.rent.deliver.dto.data.serve;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class VehicleInsuranceVO {

    @ApiModelProperty(value = "保险开始时间,只在查看发车单详情时展示")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startTime;

    @ApiModelProperty(value = "发车单投保详情展示模板，1：老模板，2：新模板")
    private Integer deliverInsuranceInfoTemplate = 1;

    //------------------------投保相关字段 start-----------------------

    @ApiModelProperty(value = "交强险承保方")
    private String compulsoryInsuranceAcceptParty;

    @ApiModelProperty(value = "交强险投保方")
    private String compulsoryInsuranceInsureParty;

    @ApiModelProperty(value = "交强险保单号")
    private String compulsoryInsurancePolicyNo;

    @ApiModelProperty(value = "交强险起保时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date compulsoryInsuranceStartDate;

    @ApiModelProperty(value = "交强险终保时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date compulsoryInsuranceEndDate;

    @ApiModelProperty(value = "商业险承保方")
    private String commercialInsuranceAcceptParty;

    @ApiModelProperty(value = "商业险投保方")
    private String commercialInsuranceInsureParty;

    @ApiModelProperty(value = "商业险起保时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date commercialInsuranceStartDate;

    @ApiModelProperty(value = "商业险终保时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date commercialInsuranceEndDate;

    //------------------------投保相关字段 end-----------------------

    //------------------------投保退保共用字段 start------------------
    @ApiModelProperty(value = "商业险保单号")
    private String commercialInsurancePolicyNo;
    //------------------------投保退保共用字段 end------------------

    //------------------------退保相关字段 start---------------------

    @ApiModelProperty(value = "是否退保,只在查看收车单详情时展示")
    private Integer isInsurance;

    @ApiModelProperty(value = "是否退保含义")
    private String isInsuranceDisplay;

    @ApiModelProperty(value = "退保时间,只在查看收车单详情时展示")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endTime;

    @ApiModelProperty(value = "暂不退保原因,只在查看收车单详情时展示")
    private Integer insuranceRemark;

    @ApiModelProperty(value = "暂不退保原因含义,只在查看收车单详情时展示")
    private String insuranceRemarkDisplay;

    @ApiModelProperty(value = "退保申请编号")
    private String surrenderApplyNo;

    @ApiModelProperty(value = "退保申请状态")
    private Integer surrenderApplyStatus;

    @ApiModelProperty(value = "退保申请状态含义")
    private String surrenderApplyStatusDisplay;

    //------------------------退保相关字段 end---------------------

}
