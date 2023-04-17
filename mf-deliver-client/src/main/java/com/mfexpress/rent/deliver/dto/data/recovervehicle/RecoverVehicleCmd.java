package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@ApiModel(value = "执行收车")
public class RecoverVehicleCmd extends BaseCmd {

    @ApiModelProperty(value = "服务单编号", required = true)
    @NotBlank(message = "服务单编号不能为空")
    private String serveNo;

    @ApiModelProperty(value = "交付单编号", required = true)
    @NotBlank(message = "交付单编号不能为空")
    private String deliverNo;

    @ApiModelProperty(value = "还车人姓名", required = true)
    @NotBlank(message = "还车人姓名不能为空")
    private String contactsName;

    @ApiModelProperty(value = "还车人手机号", required = true)
    @NotBlank(message = "还车人手机号不能为空")
    private String contactsPhone;

    @ApiModelProperty(value = "还车人身份证号", required = true)
    @NotBlank(message = "还车人身份证号不能为空")
    private String contactsCard;

    @ApiModelProperty(value = "收车时间", required = true)
    @JsonFormat(timezone = "GMT+8")
    @NotNull(message = "收车时间不能为空")
    private Date recoverVehicleTime;

    @ApiModelProperty(value = "车辆停放地", required = true)
    @NotNull(message = "车辆停放地不能为空")
    private Integer wareHouseId;

    @ApiModelProperty(value = "合照", required = true)
    @NotBlank(message = "合照不能为空")
    private String imgUrl;

    @ApiModelProperty(value = "车损费")
    private Double damageFee;

    @ApiModelProperty(value = "路边停车费")
    private Double parkFee;

    @ApiModelProperty(value = "车辆交接单", required = true)
    @NotEmpty(message = "车辆交接单不能为空")
    private List<String> handoverImgUrls;

    @ApiModelProperty(value = "交付方式")
    private Integer deliverMethod;

    @ApiModelProperty(value = "车辆ID")
    private Integer carId;

}
