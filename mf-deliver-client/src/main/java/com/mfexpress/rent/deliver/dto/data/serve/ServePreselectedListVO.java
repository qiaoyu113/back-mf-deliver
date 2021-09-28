package com.mfexpress.rent.deliver.dto.data.serve;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.mfexpress.rent.deliver.dto.data.ListVO;
import com.mfexpress.rent.deliver.dto.data.OrderCarModelVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("发车任务待预选列表数据")
public class ServePreselectedListVO extends ListVO {

    @ApiModelProperty(value = "订单id")
    private String orderId;
    @ApiModelProperty(value = "客户id")
    private Integer customerId;
    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "合同编号")
    private String contractNo;

    @ApiModelProperty(value = "提车时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date extractVehicleTime;

    @ApiModelProperty(value = "订单车型数量情况列表")
    private List<OrderCarModelVO> orderCarModelVOList;

    @ApiModelProperty(value = "车型相同待预选服务单列表")
    private List<ServePreselectedVO> servePreselectedVOList;

}
