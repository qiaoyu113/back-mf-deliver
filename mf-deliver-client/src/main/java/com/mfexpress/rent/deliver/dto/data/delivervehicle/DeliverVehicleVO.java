package com.mfexpress.rent.deliver.dto.data.delivervehicle;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class DeliverVehicleVO {

    @ApiModelProperty(value = "提车人姓名")
    private String contactsName;

    @ApiModelProperty(value = "提车人电话")
    private String contactsPhone;

    @ApiModelProperty(value = "提车人身份证号")
    private String contactsCard;

    @ApiModelProperty(value = "发车日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date deliverVehicleTime;

    @ApiModelProperty(value = "人车合照")
    private String imgUrl;

}
