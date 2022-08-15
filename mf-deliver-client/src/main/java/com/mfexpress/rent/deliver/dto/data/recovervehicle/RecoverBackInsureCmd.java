package com.mfexpress.rent.deliver.dto.data.recovervehicle;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("退保")
public class RecoverBackInsureCmd extends BaseCmd {

    @ApiModelProperty(value = "服务编号")
    private List<String> serveNoList;
    @ApiModelProperty(value = "车辆id")
    private List<Integer> carIdList;

    @ApiModelProperty(value = "是否退保", example = "0否，1是")
    private Integer isInsurance;

    @ApiModelProperty(value = "退保时间")
    @JsonFormat(timezone = "GMT+8")
    private Date insuranceTime;

    @ApiModelProperty(value = "暂不退保原因,默认传0")
    private Integer insuranceRemark;

    private Integer carServiceId;
}
