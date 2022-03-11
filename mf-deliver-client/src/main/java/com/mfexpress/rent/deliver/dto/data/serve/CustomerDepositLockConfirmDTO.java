package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@ApiModel("客户押金锁定确认")
public class CustomerDepositLockConfirmDTO {

    @NotNull
    @ApiModelProperty("服务单编号")
    private String serveNo;
    @NotNull
    @ApiModelProperty("锁定金额")
    private BigDecimal lockAmount;

    @NotNull
    @ApiModelProperty("创建人")
    private Integer creatorId;
}
