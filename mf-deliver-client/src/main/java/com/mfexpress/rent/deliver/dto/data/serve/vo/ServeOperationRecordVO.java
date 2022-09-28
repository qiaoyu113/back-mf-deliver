package com.mfexpress.rent.deliver.dto.data.serve.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hzq
 * @Package com.mfexpress.rent.deliver.dto.data.serve.vo
 * @date 2022/9/28 10:04
 * @Copyright ©
 */
@Data
@ApiModel(value = "服务单操作记录vo")
public class ServeOperationRecordVO {

    @ApiModelProperty(value = "操作类型")
    private String category;

    @ApiModelProperty(value = "操作人")
    private String operator;

    @ApiModelProperty(value = "操作时间")
    private String operationTime;

}
