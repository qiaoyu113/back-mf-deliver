package com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "收车无效命令")
public class RecoverInvalidCmd extends BaseCmd {

    @ApiModelProperty(value = "交付单号")
    private String deliverNo;
}
