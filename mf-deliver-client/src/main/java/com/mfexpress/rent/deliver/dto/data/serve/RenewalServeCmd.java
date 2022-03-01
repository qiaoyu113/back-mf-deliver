package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("服务单续约命令")
public class RenewalServeCmd {

    @ApiModelProperty(value = "服务单编号")
    @NotEmpty(message = "服务单编号不能为空")
    private String serveNo;

    @ApiModelProperty(value = "商品id")
    @NotNull(message = "商品id不能为空")
    private Integer goodsId;

    @ApiModelProperty(value = "租赁方式id")
    @NotNull(message = "租赁方式id不能为空")
    private Integer leaseModelId;

    @ApiModelProperty(value = "租金")
    @NotNull(message = "租金不能为空")
    private Double rent;

    @ApiModelProperty(value = "押金")
    @NotNull(message = "押金不能为空")
    private Double deposit;

    @ApiModelProperty(value = "租赁开始日期")
    @NotEmpty(message = "租赁开始日期不能为空")
    private String leaseBeginDate;

    @ApiModelProperty(value = "租期（月）")
    @NotNull(message = "租期不能为空")
    private Integer leaseMonths;

    @ApiModelProperty(value = "租赁结束日期")
    @NotEmpty(message = "租赁结束日期不能为空")
    private String leaseEndDate;

    @ApiModelProperty(value = "计费调整日期")
    // @NotEmpty(message = "计费调整日期不能为空")
    private String billingAdjustmentDate;

    @ApiModelProperty(value = "车牌号")
    @NotEmpty(message = "车牌号不能为空")
    private String carNum;

}
