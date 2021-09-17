package com.mfexpress.rent.deliver.dto.data.deliver;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "预选车辆信息")
public class DeliverVehicleSelectCmd {

    @NotNull(message = "车辆id不能为空")
    @ApiModelProperty(value = "车辆id")
    private Integer id;

    @ApiModelProperty(value = "车牌号")
    private String plateNumber;

    @ApiModelProperty(value = "车架号")
    private String vin;

    @ApiModelProperty(value = "是否投保")
    private Integer insuranceStatus;

    @ApiModelProperty(value = "车龄")
    private Double vehicleAge;

    @ApiModelProperty(value = "里程")
    private Double mileage;
}
