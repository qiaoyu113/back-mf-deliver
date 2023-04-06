package com.mfexpress.rent.deliver.dto.data.delivervehicle;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class DeliverVehicleCmd extends BaseCmd {

    @ApiModelProperty(value = "合照列表", required = true)
    @NotEmpty(message = "合照列表不能为空")
    private List<DeliverVehicleImgCmd> deliverVehicleImgCmdList;

    @ApiModelProperty(value = "提车人", required = true)
    @NotBlank(message = "提车人不能为空")
    private String contactsName;

    @ApiModelProperty(value = "提车人手机号", required = true)
    @NotBlank(message = "提车人手机号不能为空")
    private String contactsPhone;

    @ApiModelProperty(value = "提车人身份证号", required = true)
    @NotBlank(message = "提车人身份证号不能为空")
    private String contactsCard;

    @ApiModelProperty(value = "发车时间", required = true)
    @JsonFormat(timezone = "GMT+8")
    @NotNull(message = "发车时间不能为空")
    private Date deliverVehicleTime;

    @ApiModelProperty(value = "客户id")
    private Integer customerId;

    @ApiModelProperty(value = "车辆交接单", required = true)
    @NotEmpty(message = "车辆交接单不能为空")
    private List<String> handoverImgUrls;

    @ApiModelProperty(value = "交付方式")
    private Integer deliverMethod;

    @ApiModelProperty(value = "预计收车日期")
    private Map<String,String> expectRecoverDateMap;

}
