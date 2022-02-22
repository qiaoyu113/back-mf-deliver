package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "ElecContractCreateStatusVO 电子合同创建状态VO")
@Data
public class ElecContractCreateStatusVO {

    @ApiModelProperty(value = "创建状态，1：创建中，2：成功，3：失败")
    private Integer createStatus;
}
