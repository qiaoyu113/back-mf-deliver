package com.mfexpress.rent.deliver.dto.data.deliver.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel(value = "投保申请编号VO")
@Data
public class InsureApplyVO extends TipVO {

    @ApiModelProperty(value = "申请时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date applyDate;

    @ApiModelProperty(value = "交强险申请id")
    private String compulsoryApplyId;

    @ApiModelProperty(value = "交强险申请编号")
    private String compulsoryInsuranceApplyCode;

    @ApiModelProperty(value = "交强险申请状态")
    private Integer compulsoryInsuranceApplyStatus;

    @ApiModelProperty(value = "交强险申请状态含义")
    private String compulsoryInsuranceApplyStatusDisplay;

    @ApiModelProperty(value = "商业险申请id")
    private String commercialApplyId;

    @ApiModelProperty(value = "商业险申请编号")
    private String commercialInsuranceApplyCode;

    @ApiModelProperty(value = "商业险申请状态")
    private Integer commercialInsuranceApplyStatus;

    @ApiModelProperty(value = "商业险申请状态含义")
    private String commercialInsuranceApplyStatusDisplay;

}
