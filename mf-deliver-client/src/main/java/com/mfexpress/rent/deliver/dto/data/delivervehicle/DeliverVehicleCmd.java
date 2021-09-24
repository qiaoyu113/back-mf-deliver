package com.mfexpress.rent.deliver.dto.data.delivervehicle;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DeliverVehicleCmd {

    @ApiModelProperty(value = "合照列表")
    private List<DeliverVehicleImgCmd> deliverVehicleImgCmdList;

    @ApiModelProperty(value = "提车人")
    private String contactsName;

    @ApiModelProperty(value = "提车人手机号")
    private String contactsPhone;

    @ApiModelProperty(value = "提车人身份证号")
    private String contactsCard;

    @ApiModelProperty(value = "发车时间")
    @JsonFormat(timezone = "GMT+8")
    private Date deliverVehicleTime;
    @ApiModelProperty(value = "客户id")
    private Integer customerId;

}
