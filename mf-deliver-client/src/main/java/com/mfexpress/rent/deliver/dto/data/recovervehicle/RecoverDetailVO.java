package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("收车申请详情")
public class RecoverDetailVO {

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "合同编号")
    private String contractNo;

    @ApiModelProperty(value = "收车申请详情")
    private RecoverVehicleVO recoverVehicleVO;

    private String customerIDCardOrgSaleName;
}
