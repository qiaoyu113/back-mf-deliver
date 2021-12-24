package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel(value = "ConfirmExpireContractCmd 确认合同过期命令")
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfirmFailCmd extends BaseCmd {

    @ApiModelProperty(value = "电子交接合同全局id", required = true)
    @NotNull(message = "合同id不能为空")
    private Long contractId;

    @ApiModelProperty(value = "失败类型，2：过期；6：契约锁端返回的合同创建失败，只会出现在合同创建中的下一步", required = true)
    @NotNull(message = "失败类型不能为空")
    private Integer type;

}
