package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@ApiModel(value = "InvalidContractCmd 电子交接合同无效命令")
@Data
public class InvalidContractCmd {

    @ApiModelProperty(value = "电子交接合同全局id", required = true)
    @NotEmpty(message = "合同id不能为空")
    private Long contractId;
}
