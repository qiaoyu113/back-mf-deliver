package com.mfexpress.rent.deliver.dto.data.deliver;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class DeliverReplaceCmd {

    @ApiModelProperty(value = "服务单编号")
    private List<String> serveList;

    @ApiModelProperty(value = "更换车辆信息")
    private List<DeliverVehicleSelectCmd> deliverVehicleSelectCmd;

    private Integer carServiceId;


}
