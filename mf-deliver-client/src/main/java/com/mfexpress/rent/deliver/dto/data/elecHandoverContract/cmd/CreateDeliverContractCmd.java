package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd;

import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@ApiModel(value = "CreateDeliverContractCmd 创建发车电子交接合同命令")
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateDeliverContractCmd extends CreateElecHandoverContractCmd {

    @ApiModelProperty(value = "发车交接单信息", required = true)
    @Valid
    @NotNull(message = "发车交接单信息不能为空")
    private DeliverInfo deliverInfo;

}
