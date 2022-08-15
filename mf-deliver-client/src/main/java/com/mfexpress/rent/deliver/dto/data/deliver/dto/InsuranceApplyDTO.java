package com.mfexpress.rent.deliver.dto.data.deliver.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value = "收发车投退保申请DTO")
public class InsuranceApplyDTO {

    private Integer id;

    @ApiModelProperty(value = "交付单号")
    private String deliverNo;

    @ApiModelProperty(value = "类型，1：投保，2：退保")
    private Integer type;

    @ApiModelProperty(value = "车辆id")
    private Integer vehicleId;

    @ApiModelProperty(value = "交强险申请批量受理编号")
    private String compulsoryBatchAcceptCode;

    @ApiModelProperty(value = "商业险申请批量受理编号")
    private String commercialBatchAcceptCode;

    @ApiModelProperty(value = "交强险申请id")
    private String compulsoryApplyId;

    @ApiModelProperty(value = "交强险申请编号")
    private String compulsoryApplyCode;

    @ApiModelProperty(value = "商业险申请id")
    private String commercialApplyId;

    @ApiModelProperty(value = "商业险申请编号")
    private String commercialApplyCode;

    @ApiModelProperty(value = "交强险保单id")
    private String compulsoryPolicyId;

    @ApiModelProperty(value = "交强险保单号")
    private String compulsoryPolicyNo;

    @ApiModelProperty(value = "交强险保单来源")
    private Integer compulsoryPolicySource;

    @ApiModelProperty(value = "商业险保单id")
    private String commercialPolicyId;

    @ApiModelProperty(value = "商业险保单号")
    private String commercialPolicyNo;

    @ApiModelProperty(value = "商业险保单来源")
    private Integer commercialPolicySource;

    @ApiModelProperty(value = "删除标志位")
    private Integer delFlag;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "创建人")
    private Integer creatorId;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "修改人")
    private Integer updaterId;

}
