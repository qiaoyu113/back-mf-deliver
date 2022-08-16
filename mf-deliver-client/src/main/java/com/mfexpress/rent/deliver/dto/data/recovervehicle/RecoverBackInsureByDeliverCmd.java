package com.mfexpress.rent.deliver.dto.data.recovervehicle;


import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@ApiModel("通过交付单号退保命令")
public class RecoverBackInsureByDeliverCmd extends RecoverBackInsureCmd {

    @ApiModelProperty(value = "交付单编号", required = true)
    @NotEmpty(message = "交付单编号不能为空")
    private List<String> deliverNoList;

    @ApiModelProperty(value = "交付单")
    private List<DeliverDTO> deliverDTOList;

}
