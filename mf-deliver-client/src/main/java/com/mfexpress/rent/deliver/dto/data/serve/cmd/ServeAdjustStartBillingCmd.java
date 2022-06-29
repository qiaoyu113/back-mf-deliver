package com.mfexpress.rent.deliver.dto.data.serve.cmd;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@ApiModel(value = "服务单调整开始计费命令")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServeAdjustStartBillingCmd extends BaseCmd {

    @ApiModelProperty(value = "服务单号")
    private String serveNo;

    @ApiModelProperty(value = "交付单号")
    private String deliverNo;

    @ApiModelProperty(value = "开始计费日期")
    private Date startBillingDate;
}
