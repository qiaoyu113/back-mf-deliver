package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value = "执行收车")
public class RecoverVechicleCmd {

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;
    @ApiModelProperty(value = "还车人姓名")
    private String contactsName;
    @ApiModelProperty(value = "还车人手机号")
    private String contactsPhone;
    @ApiModelProperty(value = "还车人身份证号")
    private String contactsCard;
    @ApiModelProperty(value = "收车时间")
    @JsonFormat(timezone = "GMT+8")
    private Date recoverVehicleTime;
    @ApiModelProperty(value = "车辆停放地")
    private Integer wareHouseId;
    @ApiModelProperty(value = "合照")
    private String imgUrl;

    private Integer carServiceId;

    @ApiModelProperty(value = "车损费")
    private Double damageFee;

    @ApiModelProperty(value = "路边停车费")
    private Double parkFee;

}
