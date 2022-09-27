package com.mfexpress.rent.deliver.dto.data.serve;

import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleVO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.ElecHandoverDocVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ServeDeliverDetailVO {

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

    @ApiModelProperty(value = "发车单信息")
    private DeliverVehicleVO deliverVehicleVO;

    @ApiModelProperty(value = "电子交接单信息")
    private ElecHandoverDocVO elecHandoverDocVO;



}

