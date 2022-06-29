package com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd;

import java.util.Date;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "收车后续处理cmd")
public class RecoverVehicleProcessCmd extends BaseCmd {

    @ApiModelProperty(value = "服务单号", required = true)
    private String serveNo;

    @ApiModelProperty(value = "发车合同ID", required = true)
    private Long contactId;

    @ApiModelProperty(value = "车辆ID")
    private Integer carId;

    @ApiModelProperty(value = "交付单号")
    private String deliverNo;

    @ApiModelProperty(value = "第三方合同号")
    private String contractForeignNo;

    @ApiModelProperty(value = "收车车辆停放地")
    private Integer recoverWareHouseId;

    @ApiModelProperty(value = "收车时间")
    private Date recoverVehicleTime;

    @ApiModelProperty(value = "预期收车时间")
    private String expectRecoverDate;

    @ApiModelProperty(value = "客户ID")
    private Integer customerId;

    @ApiModelProperty(value = "服务")
    private Integer serveStatus;
}
