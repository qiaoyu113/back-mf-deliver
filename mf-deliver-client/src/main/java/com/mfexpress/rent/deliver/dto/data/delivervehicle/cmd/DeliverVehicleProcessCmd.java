package com.mfexpress.rent.deliver.dto.data.delivervehicle.cmd;

import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "发车后续处理命令")
public class DeliverVehicleProcessCmd {

    @ApiModelProperty(value = "客户ID", required = true)
    private Integer customerId;

    @ApiModelProperty(value = "ElecContractDTO 电子交接合同DTO", required = true)
    private ElecContractDTO contractDTO;
}
