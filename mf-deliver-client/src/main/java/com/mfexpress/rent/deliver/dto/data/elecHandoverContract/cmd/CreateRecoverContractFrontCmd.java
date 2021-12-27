package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.RecoverInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@ApiModel(value = "CreateRecoverContractFrontCmd adapter层创建收车电子交接合同与前端适配的命令")
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateRecoverContractFrontCmd extends BaseCmd {

    @ApiModelProperty(value = "收车交接单信息", required = true)
    @Valid
    @NotNull(message = "收车交接单信息不能为空")
    private RecoverInfo recoverInfo;

    private Integer orgId;

    private Long orderId;

}
