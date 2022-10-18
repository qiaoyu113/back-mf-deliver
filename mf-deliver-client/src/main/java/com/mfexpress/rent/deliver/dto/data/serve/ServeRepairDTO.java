package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value = "服务单维修记录DTO")
public class ServeRepairDTO {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;

    @ApiModelProperty(value = "维修单id")
    private Long maintenanceId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}
