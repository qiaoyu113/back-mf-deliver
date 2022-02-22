package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel(value = "SendSmsCmd 电子交接合同发送催签短信命令")
@Data
public class SendSmsCmd {

    @ApiModelProperty(value = "电子交接合同全局id", required = true)
    @NotNull(message = "合同id不能为空")
    private Long contractId;

}
