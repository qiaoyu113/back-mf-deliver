package com.mfexpress.rent.deliver.dto.data.deliver;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class DeliverPreselectedCmd {


    @ApiModelProperty(value = "服务单编号列表")
    private List<String> serveList;

    @ApiModelProperty(value = "预选车辆列表")
    private List<DeliverVehicleSelectCmd> deliverVehicleSelectCmdList;

}
