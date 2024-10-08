package com.mfexpress.rent.deliver.dto.data.serve.cmd;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ServeDepositPayCmd extends BaseCmd {

    @ApiModelProperty("客户id")
    @NotNull(message = "customerId不能为空")
    @Min(1L)
    private  Integer customerId;

    @ApiModelProperty("订单id")
    @NotNull(message = "orderId不能为空")
    private  Long orderId;

    @ApiModelProperty(value = "实缴押金金额")
    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("99999999.99")
    private BigDecimal paidInDepositAmount;

    @ApiModelProperty(value = "应缴押金金额")
    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("99999999.99")
    private BigDecimal payAbleDepositAmount;

    @NotNull
    @ApiModelProperty("服务单编号")
    private String serveNo;

    @ApiModelProperty(value = "原车服务单号")
    private String sourceServeNo;

    @ApiModelProperty(value = "押金支付方式")
    private Integer depositPayType;

    private Integer userId;
}
