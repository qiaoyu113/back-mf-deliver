package com.mfexpress.rent.deliver.dto.data.serve;


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
    private Integer orderId;
    @ApiModelProperty(value = "提车公司")
    private String customerName;
    @ApiModelProperty(value = "提车时间 ")
    private Date extractVehicleTime;

    @ApiModelProperty(value = "订单车型列表")
    private List<OrderCarModelVO> carModelVOList;

    @ApiModelProperty(value = "待发车数量")
    private Integer stayDeliverNum;
    @ApiModelProperty(value = "已发车数量")
    private Integer isDeliverNum;

}
