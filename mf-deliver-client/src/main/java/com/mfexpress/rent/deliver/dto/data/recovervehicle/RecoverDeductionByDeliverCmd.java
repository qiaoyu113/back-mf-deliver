package com.mfexpress.rent.deliver.dto.data.recovervehicle;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@ApiModel("违章处理")
@Data
public class RecoverDeductionByDeliverCmd extends RecoverDeductionCmd{

    @ApiModelProperty(value = "交付单编号", required = true)
    @NotEmpty(message = "交付单编号不能为空")
    private String deliverNo;

}
