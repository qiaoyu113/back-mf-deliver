package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel("服务单续约命令")
public class RenewalCmd {

    @ApiModelProperty(value = "oa合同编号")
    @NotEmpty(message = "oa合同编号不能为空")
    private String oaContractCode;

    @ApiModelProperty(value = "操作人id")
    @NotNull(message = "操作人id不能为空")
    private Integer operatorId;

    @ApiModelProperty("多个服务单续约命令")
    @NotEmpty(message = "服务单续约命令不能为空")
    @Valid
    private List<RenewalServeCmd> serveCmdList;
}
