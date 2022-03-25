package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@ApiModel("ReactivateServeCheckCmd 重新激活服务单进行发车操作时的检查命令")
@Data
@Builder
public class ReactivateServeCheckCmd {

    @ApiModelProperty(value = "服务单编号")
    private List<String> serveNoList;

    @ApiModelProperty(value = "发车日期")
    private Date deliverVehicleTime;
}
