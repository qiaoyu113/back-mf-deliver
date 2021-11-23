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
}
