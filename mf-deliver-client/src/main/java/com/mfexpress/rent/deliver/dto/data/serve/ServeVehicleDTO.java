package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("车辆信息")
public class ServeVehicleDTO {
    @ApiModelProperty(value = "车型id")
    private Integer carModelId;
    @ApiModelProperty(value = "品牌id")
    private Integer brandId;
    @ApiModelProperty(value = "租赁方式id")
    private Integer leaseModelId;
    @ApiModelProperty(value = "数量")
    private Integer num;
    @ApiModelProperty(value = "月租金")
    private BigDecimal rent;
}
