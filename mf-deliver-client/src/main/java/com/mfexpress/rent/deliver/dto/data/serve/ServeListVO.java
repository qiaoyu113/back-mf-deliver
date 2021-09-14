package com.mfexpress.rent.deliver.dto.data.serve;

import com.mfexpress.rent.deliver.dto.data.ListVO;
import com.mfexpress.rent.deliver.dto.data.OrderCarModelVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@ApiModel(value = "交付信息列表")
@Data
public class ServeListVO extends ListVO {
    @ApiModelProperty(value = "订单id")
    private Long orderId;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "合同编号")
    private String contractNo;

    @ApiModelProperty(value = "提车日期")
    private Date extractVehicleTime;

    @ApiModelProperty(value = "订单车型列表")
    private List<OrderCarModelVO> carModelVOList;

    @ApiModelProperty(value = "租赁服务单列表")
    private List<ServeVO> serveVOList;


}
