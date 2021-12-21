package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel(value = "ContractQry 电子交接合同查询命令")
@Data
public class ContractQry {

    @ApiModelProperty(value = "电子交接合同全局id", required = true)
    @NotNull(message = "合同id不能为空")
    private Long contractId;

}
