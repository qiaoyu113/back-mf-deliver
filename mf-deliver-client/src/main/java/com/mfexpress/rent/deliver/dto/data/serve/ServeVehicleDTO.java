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

    @ApiModelProperty(value = "订单下的商品id")
    private Integer goodsId;

    @ApiModelProperty(value = "合同下的商品id")
    private Integer contractCommodityId;

    @ApiModelProperty(value = "合同全局id")
    private Long contractId;

    @ApiModelProperty(value = "oa合同编号")
    private String oaContractCode;

    @ApiModelProperty(value = "押金")
    private Double deposit;

    @ApiModelProperty(value = "租赁开始日期")
    private String leaseBeginDate;

    @ApiModelProperty(value = "租赁期限（月）")
    private Integer leaseMonths;

    @ApiModelProperty(value = "租赁期限（天）")
    private Integer leaseDays;

    @ApiModelProperty(value = "租赁结束日期")
    private String leaseEndDate;

}
