package com.mfexpress.rent.deliver.dto.data.serve;

import com.mfexpress.rent.deliver.dto.data.ListQry;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@ApiModel("订单号查询")
public class ServeQryListCmd extends ListQry {

    @NotEmpty(message = "订单编号不能为空")
    @ApiModelProperty(value = "订单编号")
    private Long orderId;



}
