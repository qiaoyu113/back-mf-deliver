package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("客户押金锁定列表")
public class CustomerDepositLockListDTO {

    @ApiModelProperty("服务单编号")
    private String serveNo;

    @ApiModelProperty("车牌号")
    private String vehicleNum;

    @ApiModelProperty("品牌型号")
    private Integer brandId;

    @ApiModelProperty("车型")
    private Integer modelId;

    @ApiModelProperty("应缴押金")
    private BigDecimal payableDeposit;

    @ApiModelProperty("实缴押金")
    private BigDecimal paidInDeposit;

    @ApiModelProperty("发车日期")
    private String deliverVehicleDate;


}
