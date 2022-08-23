package com.mfexpress.rent.deliver.dto.data.deliver.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "后市场端返回的批量退保申请DTO")
public class RecoverBatchSurrenderApplyDTO {

    @ApiModelProperty(value = "退保批量受理id")
    private String batchId;

    @ApiModelProperty(value = "退保批量受理编号")
    private String batchCode;

    @ApiModelProperty(value = "车辆id")
    private Integer vehicleId;

    @ApiModelProperty(value = "退保申请id")
    private String applyId;

    @ApiModelProperty(value = "退保申请编号")
    private String applyCode;

    @ApiModelProperty(value = "保险公司id")
    private Integer insuranceCompanyId;

    @ApiModelProperty(value = "保险公司")
    private String insuranceCompany;
}
