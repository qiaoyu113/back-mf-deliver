package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


@Data
@ApiModel(value = "租赁服务单")
public class ServeVO {

    @ApiModelProperty(value = "订单id")
    private Integer orderId;
    @ApiModelProperty(value = "提车公司")
    private String customerName;
    @ApiModelProperty(value = "租赁服务单编号")
    private String serveNo;
    @ApiModelProperty(value = "租赁方式id")
    private Integer leaseModelId;
    @ApiModelProperty(value = "租赁方式描述")
    private String leaseModelDisplay;
    @ApiModelProperty(value = "车型id")
    private Integer carModelId;
    @ApiModelProperty(value = "交付单编号")
    private String deliverNo;


    @ApiModelProperty(value = "品牌id")
    private Integer brandId;
    @ApiModelProperty(value = "品牌车型描述")
    private String brandModelDisplay;
    @ApiModelProperty(value = "车牌号")
    private String carNum;
    @ApiModelProperty(value = "车架号")
    private String frameNum;
    @ApiModelProperty(value = "发车日期")
    private Date deliverVehicleTime;
    @ApiModelProperty(value = "预选状态")
    private Integer isPreselected;
    @ApiModelProperty(value = "预选状态描述")
    private String isPreselectedDisplay;
    @ApiModelProperty(value = "验车状态")
    private Integer isCheck;
    @ApiModelProperty(value = "验车状态描述")
    private String isCheckDisplay;
    @ApiModelProperty(value = "保险状态")
    private Integer isInsurance;
    @ApiModelProperty(value = "保险状态描述")
    private String isInsuranceDisplay;
    @ApiModelProperty(value = "交付状态")
    private Integer deliverStatus;
    @ApiModelProperty(value = "里程")
    private Double mileage;
    @ApiModelProperty(value = "车龄")
    private Double vehicleAge;
}


