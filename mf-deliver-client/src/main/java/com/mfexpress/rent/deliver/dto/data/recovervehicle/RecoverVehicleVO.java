package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("收车单信息")
public class RecoverVehicleVO {

    @ApiModelProperty(value = "租赁服务单编号")
    private String serveNo;
    @ApiModelProperty(value = "客户姓名")
    private String customerName;
    @ApiModelProperty(value = "合同编号")
    private String contractNo;
    @ApiModelProperty(value = "车辆id")
    private Integer carId;
    @ApiModelProperty(value = "车牌号")
    private String carNum;
    @ApiModelProperty(value = "车型id")
    private Integer carModelId;
    @ApiModelProperty(value = "客户id")
    private Integer customerId;
    @ApiModelProperty(value = "品牌id")
    private Integer brandId;
    @ApiModelProperty(value = "品牌车型描述")
    private String brandModelDisplay;
    @ApiModelProperty(value = "租赁方式id")
    private Integer leaseModelId;
    @ApiModelProperty(value = "租赁方式描述")
    private String leaseModelDisplay;
    @ApiModelProperty(value = "车架号")
    private String frameNum;
    @ApiModelProperty(value = "发车时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date deliverVehicleTime;
    @ApiModelProperty(value = "收车时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date recoverVehicleTime;
    @ApiModelProperty(value = "预计收车时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expectRecoverTime;
    @ApiModelProperty(value = "验车状态")
    private Integer isCheck;
    @ApiModelProperty(value = "保险状态")
    private Integer isInsurance;
    @ApiModelProperty(value = "处理违章状态")
    private Integer isDeduction;
    @ApiModelProperty(value = "交付状态")
    private Integer deliverStatus;


}
