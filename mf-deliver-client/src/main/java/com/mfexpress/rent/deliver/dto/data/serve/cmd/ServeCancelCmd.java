package com.mfexpress.rent.deliver.dto.data.serve.cmd;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "服务单取消命令")
public class ServeCancelCmd extends BaseCmd {

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;
}
