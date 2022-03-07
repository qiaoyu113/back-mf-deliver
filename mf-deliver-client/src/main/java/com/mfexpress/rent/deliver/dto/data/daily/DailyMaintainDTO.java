package com.mfexpress.rent.deliver.dto.data.daily;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("日报维修调整")
public class DailyMaintainDTO {

    @ApiModelProperty("服务单编号")
    private String serveNo;
    @ApiModelProperty("维修开始或结束日期")
    private String maintainDate;

    @ApiModelProperty(value = "维修开始或结束标识",example = "开始：true")
    private Boolean maintainFlag;


}
