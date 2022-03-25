package com.mfexpress.rent.deliver.dto.data.deliver;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class DeliverPreselectedCmd {

    @ApiModelProperty(value = "客户Id")
    @NotNull(message = "客户id不能为空")
    private Integer customerId;

    @ApiModelProperty(value = "服务单编号列表")
    @NotEmpty(message = "服务单编号不能为空")
    private List<String> serveList;

    @ApiModelProperty(value = "预选车辆列表")
    @NotEmpty(message = "预选车辆不能为空")
    private List<DeliverVehicleSelectCmd> deliverVehicleSelectCmdList;

    private Integer carServiceId;

}
