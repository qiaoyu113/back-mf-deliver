package com.mfexpress.rent.deliver.dto.data.serve.cmd;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "服务单调整检查")
public class ServeAdjustCheckCmd extends BaseCmd {

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;
}
