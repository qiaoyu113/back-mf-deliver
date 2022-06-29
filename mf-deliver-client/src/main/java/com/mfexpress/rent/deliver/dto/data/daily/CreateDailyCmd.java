package com.mfexpress.rent.deliver.dto.data.daily;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "收发车操作日志")
public class CreateDailyCmd {

    @ApiModelProperty(value = "服务单号集合")
    private List<String> serveNoList;

    @ApiModelProperty(value = "发车/收车时间")
    private Date deliverRecoverDate;

    @ApiModelProperty(value = "收发车标志 1-发车;0-收车")
    private Integer deliverFlag;
}
