package com.mfexpress.rent.deliver.dto.data.deliver.cmd;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(value = "交付单取消命令 CMD")
public class DeliverCancelCmd extends BaseCmd {

    @ApiModelProperty(value = "服务单号")
    private String serveNo;
}
