package com.mfexpress.rent.deliver.dto.data.serve;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("ServeAllLeaseTermAmountVO 服务单信息和在其租赁周期中产生的计费信息聚合而成的对象")
@Data
public class ServeAllLeaseTermAmountVO {

    @ApiModelProperty(value = "租赁服务单编号")
    private String serveNo;

    @ApiModelProperty(value = "车辆id")
    private Integer carId;

    @ApiModelProperty(value = "车牌号")
    private String plateNumber;

    @ApiModelProperty(value = "车型id")
    private Integer carModelId;

    @ApiModelProperty(value = "车型含义")
    private String carModelDisplay;

    @ApiModelProperty(value = "租赁价格")
    private String rentFee;

    @ApiModelProperty(value = "服务费")
    private String serviceFee;

    @ApiModelProperty(value = "月租金 = 租赁价格+服务费")
    private String rent;

    @ApiModelProperty(value = "押金")
    private String deposit;

    @ApiModelProperty(value = "总欠费")
    private String totalArrears;

    @ApiModelProperty(value = "租赁方式id")
    private Integer leaseModelId;

    @ApiModelProperty(value = "租赁方式id含义")
    private String leaseModelDisplay;

    @ApiModelProperty(value = "服务单状态")
    private Integer serveStatus;

    @ApiModelProperty(value = "服务单状态含义")
    private String serveStatusDisplay;

    @ApiModelProperty(value = "客户id")
    private Integer customerId;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "oa合同编号")
    private String oaContractCode;

    @ApiModelProperty(value = "所属大区id")
    private Integer orgId;

    @ApiModelProperty(value = "所属大区名称")
    private String orgName;

    @ApiModelProperty(value = "收车时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date recoverVehicleTime;

    @ApiModelProperty(value = "预计收车时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expectRecoverDate;

    private String expectRecoverDateChar;

    @ApiModelProperty(value = "是否允许重新激活，1：真，0：假")
    private Integer enableReactivate;

    @ApiModelProperty(value = "合同商品id")
    private Integer contractCommodityId;

}
