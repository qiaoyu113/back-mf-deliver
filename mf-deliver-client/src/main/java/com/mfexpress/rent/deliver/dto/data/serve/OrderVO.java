package com.mfexpress.rent.deliver.dto.data.serve;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class OrderVO {

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "合同编号")
    private String contractNo;

    @ApiModelProperty(value = "提车时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date deliveryDate;

    @ApiModelProperty(value = "租赁方式")
    private String purposeDisplay;

}
