package com.mfexpress.rent.deliver.dto.data.serve;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class VehicleVO {

    @ApiModelProperty(value = "车牌号")
    private String carNum;

    @ApiModelProperty(value = "车型id")
    private Integer brandId;

    @ApiModelProperty(value = "车型名称")
    private String brandModelDisplay;

    @ApiModelProperty(value = "车架号")
    private String vin;

    @ApiModelProperty(value = "公里数")
    private Double mileage;

    @ApiModelProperty(value = "车龄")
    private Double vehicleAge;

    @ApiModelProperty(value = "发车日期,只在查看收车单详情时展示")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date deliverVehicleTime;

    @ApiModelProperty(value = "预计还车日期,只在查看收车单详情时展示")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expectRecoverTime;

    @ApiModelProperty(value = "车辆运营模式")
    private Integer vehicleBusinessMode;

    @ApiModelProperty(value = "车辆运营模式描述")
    private String vehicleBusinessModeDisplay;

}
