package com.mfexpress.rent.deliver.dto.data.serve.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hzq
 * @Package com.mfexpress.rent.deliver.dto.data.serve.cmd
 * @date 2022/9/28 09:33
 * @Copyright ©
 */
@Data
@ApiModel(value = "终止租赁服务单cmd")
public class TerminationServiceCmd {

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;

}
