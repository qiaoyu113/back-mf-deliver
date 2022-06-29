package com.mfexpress.rent.deliver.dto.data.serve.cmd;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "修改服务单实缴金额cmd")
public class ServePaidInDepositUpdateCmd {

    @ApiModelProperty(value = "服务单号")
    private String serveNo;

    @ApiModelProperty(value = "改变的押金金额：正数为+，负数为-")
    private BigDecimal chargeDepositAmount;
}
