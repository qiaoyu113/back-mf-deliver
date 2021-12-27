package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel(value = "ContractListQry 电子交接合同列表查询命令")
@Data
public class ContractListQry {

    @ApiModelProperty(value = "订单编号", required = true)
    @NotEmpty(message = "订单编号不能为空")
    private String orderId;

    @ApiModelProperty(value = "收发车类型，1:发车，2:收车", required = true)
    @NotNull(message = "收发车类型不能为空")
    private Integer deliverType;

    @ApiModelProperty(value = "当前页数", required = true)
    @NotNull(message = "当前页不能为空")
    @Min(value = 1, message = "分页参数错误")
    private Integer page;

    @ApiModelProperty(value = "每页多少条", required = true)
    @NotNull(message = "每页多少条不能为空")
    @Min(value = 1, message = "分页参数错误")
    private Integer limit;

}
