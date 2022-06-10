package com.mfexpress.rent.deliver.dto.data.serve.qry;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(value = "替换单调整记录查询")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServeAdjustRecordQry {
    
    @ApiModelProperty(value = "服务单号")
    private String serveNo;
}
