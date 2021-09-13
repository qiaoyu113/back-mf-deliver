package com.mfexpress.rent.deliver.dto.data.recovervehicle;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("申请收车详情页筛选信息")
public class RecoverApplyQryCmd {

    @ApiModelProperty(value = "订单编号")
    private Integer orderId;

    @ApiModelProperty(value = "合同编号")
    private String contractNo;

    @ApiModelProperty(value = "车辆型号id")
    private Integer carModelId;
    @ApiModelProperty(value = "品牌id")
    private Integer brandId;
    @ApiModelProperty(value = "发车时间段start")
    private Date startDeliverTime;
    @ApiModelProperty(value = "发车时间段end")
    private Date endDeliverTime;
}
