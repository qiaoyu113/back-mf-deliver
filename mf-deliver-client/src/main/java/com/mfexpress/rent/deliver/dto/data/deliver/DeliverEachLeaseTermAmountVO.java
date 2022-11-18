package com.mfexpress.rent.deliver.dto.data.deliver;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("DeliverEachLeaseTermAmountVO 交付单部分信息和在其租赁周期中产生的计费、账单信息聚合而成的对象")
@Data
public class DeliverEachLeaseTermAmountVO {

    @ApiModelProperty(value = "交付单编号")
    private String deliverNo;

    @ApiModelProperty(value = "客户id")
    private Integer customerId;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "oa合同编号")
    private String oaContractCode;

    @ApiModelProperty(value = "车辆id")
    private Integer carId;

    @ApiModelProperty(value = "车牌号")
    private String plateNumber;

    @ApiModelProperty(value = "车型id")
    private Integer carModelId;

    @ApiModelProperty(value = "车型含义")
    private String carModelDisplay;

    @ApiModelProperty(value = "租赁方式id")
    private Integer leaseModelId;

    @ApiModelProperty(value = "租赁方式id含义")
    private String leaseModelDisplay;

    @ApiModelProperty(value = "实时租金")
    private String realTimeRentFee;

    @ApiModelProperty(value = "租期/租赁月份")
    private String leaseMonth;

    @ApiModelProperty(value = "租赁月份开始日期")
    private String leaseMonthStartDay;

    @ApiModelProperty(value = "租赁月份结束日期")
    private String leaseMonthEndDay;

    @ApiModelProperty(value = "具体租赁周期")
    private String leaseMonthStartWithEndDay;

    @ApiModelProperty(value = "每月的一个租金")
    private String unitPrice;

    @ApiModelProperty(value = "待还金额")
    private String unpaidAmount;

    @ApiModelProperty(value = "回款状态")
    private Integer repaymentStatus;

    @ApiModelProperty(value = "回款状态含义")
    private String repaymentStatusDisplay;

    @ApiModelProperty(value = "累计调账金额")
    private String totalAdjustAmount;

    @ApiModelProperty(value = "发车电子交接单")
    private String deliverVehicleElecFileUrl;

    @ApiModelProperty(value = "收车电子交接单")
    private String recoverVehicleElecFileUrl;

}
