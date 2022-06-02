package com.mfexpress.rent.deliver.dto.data.serve.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "原车服务单押金转移到替换车服务单CMD")
public class ServeDepositSourceToReplaceCmd {

    @ApiModelProperty(value = "原车服务单号")
    private String sourceServeNo;

    @ApiModelProperty(value = "替换车服务单号")
    private String replaceServeNo;
}
