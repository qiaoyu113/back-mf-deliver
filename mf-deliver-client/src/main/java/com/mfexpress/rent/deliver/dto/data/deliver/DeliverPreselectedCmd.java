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

    @ApiModelProperty(value = "二次操作标志位，1：真，0：假")
    @NotNull(message = "操作标志位不能为空")
    private Integer secondOperationFlag;

    @ApiModelProperty(value = "预选车辆时所要求的车辆保险状态，1：不限制，2：只能选择交强险在保，而商业险不在保的车辆")
    @NotNull(message = "合同所要求车辆保险状态不能为空")
    private Integer vehicleInsureRequirement;

}
