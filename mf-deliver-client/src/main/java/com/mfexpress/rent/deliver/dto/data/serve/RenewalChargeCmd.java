package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("续约计费")
public class RenewalChargeCmd {
    @ApiModelProperty("续约目标日期")
    private String renewalDate;

    @ApiModelProperty("续约新价格")
    private BigDecimal rent;

    @ApiModelProperty("新价格生效日期")
    private String rentEffectDate;

    @ApiModelProperty("单价是否变化")
    private Boolean effectFlag;

    @ApiModelProperty("服务单号")
    private String serveNo;

    @ApiModelProperty("客户")
    private Integer customerId;

    @ApiModelProperty("车辆id")
    private Integer vehicleId;

    @ApiModelProperty("操作人")
    private Integer createId;

    @ApiModelProperty("计费所属车辆的交付单编号")
    private String deliverNo;
}
