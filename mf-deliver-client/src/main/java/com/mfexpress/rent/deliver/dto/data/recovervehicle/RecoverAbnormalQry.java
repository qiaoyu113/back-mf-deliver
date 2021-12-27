package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@ApiModel("查询异常收车信息命令")
@Data
public class RecoverAbnormalQry {

    @ApiModelProperty(value = "租赁服务单编号", required = true)
    @NotEmpty(message = "租赁服务单编号不能为空")
    private String serveNo;

}
