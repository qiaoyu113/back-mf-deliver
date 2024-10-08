package com.mfexpress.rent.deliver.dto.data.deliver;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel(value = "DeliverContractSingingCmd 改变交付单合同签署状态为签署中的命令")
@Data
public class DeliverContractSigningCmd {

    @ApiModelProperty(value = "交付单编号", required = true)
    @NotEmpty(message = "交付号不能为空")
    private List<String> deliverNos;

    @ApiModelProperty(value = "交接类型", required = true)
    @NotNull(message = "交接类型不能为空")
    private Integer deliverType;

}
