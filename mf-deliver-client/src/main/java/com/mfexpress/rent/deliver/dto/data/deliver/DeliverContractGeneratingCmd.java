package com.mfexpress.rent.deliver.dto.data.deliver;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel(value = "DeliverContractSingingCmd 改变交付单合同签署状态为生成中的命令")
@Data
public class DeliverContractGeneratingCmd {

    @ApiModelProperty(value = "交付单编号", required = true)
    @NotEmpty(message = "服务编号不能为空")
    private List<String> serveNos;

    @ApiModelProperty(value = "交接类型", required = true)
    @NotNull(message = "交接类型不能为空")
    private Integer deliverType;

}
