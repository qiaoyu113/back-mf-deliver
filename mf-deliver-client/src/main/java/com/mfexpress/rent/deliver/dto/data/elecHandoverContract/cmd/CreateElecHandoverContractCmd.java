package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverImgInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@ApiModel(value = "CreateElecHandoverContractCmd 创建电子交接合同命令")
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateElecHandoverContractCmd extends BaseCmd {

    @ApiModelProperty(value = "收发车类型，1:发车，2:收车")
    //@NotNull(message = "收发车类型不能为空")
    private Integer deliverType;

    @ApiModelProperty(value = "人车合照", required = true)
    @Valid
    @NotEmpty(message = "人车合照不能为空")
    private List<DeliverImgInfo> deliverImgInfos;

    private Integer orgId;

    private Long orderId;

}


