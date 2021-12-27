package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@ApiModel(value = "DeliverInfo 发车交接单信息")
@Data
public class DeliverInfo {

    @ApiModelProperty(value = "提车人名称", required = true)
    @NotEmpty(message = "提车人名称不能为空")
    private String contactsName;

    @ApiModelProperty(value = "提车人手机号", required = true)
    @NotEmpty(message = "提车人手机号不能为空")
    private String contactsPhone;

    @ApiModelProperty(value = "提车人身份证号", required = true)
    @NotEmpty(message = "提车人身份证号不能为空")
    private String contactsCard;

    @ApiModelProperty(value = "发车日期", required = true)
    @NotNull(message = "发车日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date deliverVehicleTime;
}
