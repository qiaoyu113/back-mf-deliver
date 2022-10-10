package com.mfexpress.rent.deliver.dto.data.recovervehicle.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "退保编号VO")
public class SurrenderApplyInfoVO {

    @ApiModelProperty(value = "退保申请编号")
    private String surrenderApplyCode;

    @ApiModelProperty(value = "退保公司")
    private String insuranceCompany;

}
