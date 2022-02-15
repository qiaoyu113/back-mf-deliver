package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@ApiModel("替换车服务单续约命令")
public class RenewalReplaceServeCmd {

    /*@ApiModelProperty(value = "oa合同编号")
    @NotEmpty(message = "oa合同编号不能为空")
    private String oaContractCode;*/

    @ApiModelProperty(value = "操作人id")
    @NotNull(message = "操作人id不能为空")
    private Integer operatorId;

    @ApiModelProperty(value = "k:服务单号,v:替换车服务单号")
    @NotEmpty(message = "替换车服务单号map不能为空")
    private Map<String, String> serveNoWithReplaceServeNoMap;
}
