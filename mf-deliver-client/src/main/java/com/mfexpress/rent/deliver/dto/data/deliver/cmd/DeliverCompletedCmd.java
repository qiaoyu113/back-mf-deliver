package com.mfexpress.rent.deliver.dto.data.deliver.cmd;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "已发车CMD")
public class DeliverCompletedCmd extends BaseCmd {

    @ApiModelProperty(value = "交付单号")
    private String deliverNo;
}
