package com.mfexpress.rent.deliver.dto.data.serve;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel("ReactivateServeCmd 重新激活服务单命令")
@Data
@EqualsAndHashCode(callSuper = true)
public class ReactivateServeCmd extends BaseCmd {

    @ApiModelProperty(value = "服务单编号", required = true)
    @NotEmpty(message = "服务单编号不能为空")
    private String serveNo;

    @ApiModelProperty(value = "激活原因", required = true)
    @NotNull(message = "激活原因不能为空")
    private Integer reason;

    @ApiModelProperty(value = "备注")
    private String remark;

    private String deliverNo;

}
