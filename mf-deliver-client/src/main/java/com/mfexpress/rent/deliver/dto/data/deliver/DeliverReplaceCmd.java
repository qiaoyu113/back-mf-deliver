package com.mfexpress.rent.deliver.dto.data.deliver;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DeliverReplaceCmd {

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;

    @ApiModelProperty(value = "更换车辆信息")
    private DeliverVehicleSelectCmd deliverVehicleSelectCmd;


}
