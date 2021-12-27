package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@ApiModel(value = "DeliverInfo 收车交接单信息")
@Data
public class RecoverInfo {

    @ApiModelProperty(value = "服务单号", required = true)
    @NotEmpty(message = "服务单号不能为空")
    private String serveNo;

    @ApiModelProperty(value = "还车人姓名", required = true)
    @NotEmpty(message = "还车人姓名不能为空")
    private String contactsName;

    @ApiModelProperty(value = "还车人手机号", required = true)
    @NotEmpty(message = "还车人手机号不能为空")
    private String contactsPhone;

    @ApiModelProperty(value = "还车人身份证号", required = true)
    @NotEmpty(message = "还车人身份证号不能为空")
    private String contactsCard;

    @ApiModelProperty(value = "收车时间", required = true)
    @NotNull(message = "收车时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date recoverVehicleTime;

    @ApiModelProperty(value = "车损费", required = true)
    @NotNull(message = "车损费不能为空")
    private Double damageFee;

    @ApiModelProperty(value = "路边停车费")
    private Double parkFee;

    @ApiModelProperty(value = "车辆停放地", required = true)
    @NotNull(message = "车辆停放地不能为空")
    private Integer wareHouseId;

    @ApiModelProperty(value = "车辆停放地含义")
    private String wareHouseDisplay;

    @ApiModelProperty(value = "合照", required = true)
    @NotEmpty(message = "合照不能为空")
    private String imgUrl;

    private String deliverNo;

    @ApiModelProperty(value = "车辆id")
    private Integer carId;

    @ApiModelProperty(value = "车牌号")
    private String carNum;

}
