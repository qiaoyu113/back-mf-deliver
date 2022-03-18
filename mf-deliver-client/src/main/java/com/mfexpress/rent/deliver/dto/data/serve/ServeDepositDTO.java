package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@ApiModel("服务单押金信息")
@Data
public class ServeDepositDTO {

    @ApiModelProperty("服务单编号")
    private String serveNo;


    @ApiModelProperty("客户id")
    private Integer customerId;

    @ApiModelProperty("管理区id")
    private Integer orgId;

    @ApiModelProperty("租赁方式id")
    private Integer leaseModelId;

    @ApiModelProperty("车牌号")
    private String vehicleNum;

    @ApiModelProperty("品牌id")
    private Integer brandId;

    @ApiModelProperty("服务单状态")
    private Integer status;

    @ApiModelProperty("服务单状态描述")
    private String statusDisplay;

    @ApiModelProperty("发车时间")
    private String deliverVehicleDate;

    @ApiModelProperty("收车时间")
    private String recoverVehicleDate;
    @ApiModelProperty("应缴押金")
    private BigDecimal payableDeposit;
    @ApiModelProperty("实缴押金")
    private BigDecimal paidInDeposit;
    @ApiModelProperty("维修费用确认标识")
    private Boolean maintainFeeConfirmFlag;
    @ApiModelProperty("收车费用确认标识")
    private Boolean recoverFeeConfirmFlag;

}
