package com.mfexpress.rent.deliver.dto.data.serve.cmd;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "撤销重新激活命令")
public class UndoReactiveServeCmd extends BaseCmd {

    @ApiModelProperty(value = "服务单编号", required = true)
    @NotBlank(message = "服务单编号不能为空")
    private String serveNo;

}
