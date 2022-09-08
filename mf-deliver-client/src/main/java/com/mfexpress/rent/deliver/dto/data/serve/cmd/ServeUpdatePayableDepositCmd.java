package com.mfexpress.rent.deliver.dto.data.serve.cmd;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(value = "服务单调整应缴押金命令")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServeUpdatePayableDepositCmd extends BaseCmd {

    @ApiModelProperty(value = "服务单号")
    private String serveNo;

    @ApiModelProperty(value = "押金金额")
    private BigDecimal depositAmount;
}
