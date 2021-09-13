package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("生成租赁服务单")
public class ServeAddCmd {

    @ApiModelProperty(value = "订单id")
    private Integer orderId;

    @ApiModelProperty("车辆信息")
    private List<ServeVehicleDTO> serveVehicleCmdList;
}
