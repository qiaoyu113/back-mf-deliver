package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class VehicleValidationVO {

    @ApiModelProperty(value = "验车标志位")
    private boolean checkFlag;

}
