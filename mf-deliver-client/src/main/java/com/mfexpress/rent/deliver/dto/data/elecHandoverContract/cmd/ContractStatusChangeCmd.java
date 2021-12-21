package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@ApiModel(value = "ContractStatusChangeCmd 合同状态改变命令")
@Data
public class ContractStatusChangeCmd {

    /*@ApiModelProperty(value = "合同状态")
    private Integer status;*/

    @ApiModelProperty(value = "本地合同id")
    private Long contractId;

    @ApiModelProperty(value = "三方的合同编号", required = true)
    @NotEmpty(message = "外部的合同编号不能为空")
    private String contractForeignNo;

    @ApiModelProperty(value = "失败原因")
    private Integer failureReason;

    @ApiModelProperty(value = "电子交接单pdf文件map")
    private Map<String, String> docPdfUrlMap;

}
