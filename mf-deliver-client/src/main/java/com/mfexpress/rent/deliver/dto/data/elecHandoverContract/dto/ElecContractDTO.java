package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel(value = "ElecContractDTO 电子交接合同DTO")
@Data
public class ElecContractDTO {

    @ApiModelProperty(value = "")
    private Integer id;

    @ApiModelProperty(value = "")
    private Long contractId;

    @ApiModelProperty(value = "")
    private String contractShowNo;

    @ApiModelProperty(value = "")
    private String deliverNos;

    @ApiModelProperty(value = "")
    private Integer deliverType;

    @ApiModelProperty(value = "")
    private String contractForeignNo;

    @ApiModelProperty(value = "")
    private String contactsName;

    @ApiModelProperty(value = "")
    private String contactsPhone;

    @ApiModelProperty(value = "")
    private String contactsCard;

    @ApiModelProperty(value = "")
    private Date deliverVehicleTime;

    @ApiModelProperty(value = "")
    private Date recoverVehicleTime;

    @ApiModelProperty(value = "")
    private String plateNumberWithImgs;

    @ApiModelProperty(value = "")
    private Double recoverDamageFee;

    @ApiModelProperty(value = "")
    private Double recoverParkFee;

    @ApiModelProperty(value = "")
    private Integer recoverWareHouseId;

    @ApiModelProperty(value = "")
    private Integer status;

    @ApiModelProperty(value = "")
    private Integer failureReason;

    @ApiModelProperty(value = "")
    private String failureMsg;

    @ApiModelProperty(value = "")
    private Integer orgId;

    @ApiModelProperty(value = "")
    private Integer cityId;

    @ApiModelProperty(value = "")
    private Integer isShow;

    @ApiModelProperty(value = "")
    private Integer sendSmsCount;

    @ApiModelProperty(value = "")
    private String sendSmsDate;

    @ApiModelProperty(value = "")
    private Date createTime;

    @ApiModelProperty(value = "")
    private Integer creatorId;

    @ApiModelProperty(value = "")
    private Date updateTime;

    @ApiModelProperty(value = "")
    private Integer updaterId;

}
