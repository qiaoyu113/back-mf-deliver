package com.mfexpress.rent.deliver.dto.data.serve;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("ServeToRenewalVO 合同续约时返回的服务单VO")
@Data
public class ServeToRenewalVO {

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;

    @ApiModelProperty(value = "OA合同编号")
    private String oaContractCode;

    @ApiModelProperty(value = "商品id")
    private Integer goodsId;

    @ApiModelProperty(value = "车牌号")
    private String carNum;

    @ApiModelProperty(value = "车型")
    private String brandDisplay;

    @ApiModelProperty(value = "租金")
    private String rent;

    @ApiModelProperty(value = "押金")
    private String deposit;

    @ApiModelProperty(value = "发车日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date deliverVehicleTime;

    @ApiModelProperty(value = "租赁天数")
    private String leaseDays;

    @ApiModelProperty(value = "预计收车日期/租赁结束日期")
    private Date leaseEndDate;

    @ApiModelProperty(value = "租赁方式")
    private String leaseModelDisplay;

    @ApiModelProperty(value = "状态")
    private String statusDisplay;

}
