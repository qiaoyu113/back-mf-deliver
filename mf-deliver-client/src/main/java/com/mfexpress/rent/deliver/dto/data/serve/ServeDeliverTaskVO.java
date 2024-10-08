package com.mfexpress.rent.deliver.dto.data.serve;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.mfexpress.rent.deliver.dto.data.OrderCarModelVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;


@ApiModel("服务单发车任务数据")
@Data
public class ServeDeliverTaskVO {
    @ApiModelProperty(value = "订单id")
    private String orderId;
    @ApiModelProperty(value = "提车公司")
    private String customerName;
    @ApiModelProperty(value = "提车时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date extractVehicleTime;

    @ApiModelProperty(value = "合同编号")
    private String contractNo;

    @ApiModelProperty(value = "订单车型列表")
    private List<OrderCarModelVO> carModelVOList;

    @ApiModelProperty(value = "待发车数量")
    private Integer stayDeliverNum;
    @ApiModelProperty(value = "已发车数量")
    private Integer isDeliverNum;

}
