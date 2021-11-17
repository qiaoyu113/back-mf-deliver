package com.mfexpress.rent.deliver.dto.data.serve;

import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ServeRecoverDetailVO {

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;

    @ApiModelProperty(value = "订单信息")
    private OrderVO orderVO;

    @ApiModelProperty(value = "车辆信息")
    private VehicleVO vehicleVO;

    @ApiModelProperty(value = "验车信息")
    private VehicleValidationVO vehicleValidationVO;

    @ApiModelProperty(value = "保险信息")
    private VehicleInsuranceVO vehicleInsuranceVO;

    @ApiModelProperty(value = "违章信息")
    private ViolationInfoVO violationInfoVO;

    @ApiModelProperty(value = "收车单信息")
    private RecoverVehicleVO recoverVehicleVO;

}
