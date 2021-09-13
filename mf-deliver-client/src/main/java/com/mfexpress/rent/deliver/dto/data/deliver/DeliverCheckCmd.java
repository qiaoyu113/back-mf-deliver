package com.mfexpress.rent.deliver.dto.data.deliver;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DeliverCheckCmd {

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;
}
