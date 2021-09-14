package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("订单生成服务单")
public class ServeAddDTO {

    @ApiModelProperty(value = "订单id")
    private Long orderId;
    @ApiModelProperty
    private Integer customerId;
    @ApiModelProperty(value = "车辆信息")
    private List<ServeVehicleDTO> vehicleDTOList;
}
