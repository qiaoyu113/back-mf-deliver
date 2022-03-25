package com.mfexpress.rent.deliver.dto.data.serve;

import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.ElecHandoverDocVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverAbnormalVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ServeRecoverDetailVO {

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;

    @ApiModelProperty(value = "交付单编号")
    private String deliverNo;

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

    @ApiModelProperty(value = "电子交接单信息")
    private ElecHandoverDocVO elecHandoverDocVO;

    /*@ApiModelProperty(value = "异常收车信息")
    private RecoverAbnormalVO recoverAbnormalVO;*/

    @ApiModelProperty(value = "异常收车标志位，1：是，0否。")
    private Integer recoverAbnormalFlag;

}
