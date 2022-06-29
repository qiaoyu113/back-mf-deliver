package com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "收车验车判断")
public class RecoverCheckJudgeCmd {

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;
}
