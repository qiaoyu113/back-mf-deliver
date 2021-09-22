package com.mfexpress.rent.deliver.dto.data.serve;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class ServePreselectedDTO {
    @ApiModelProperty(value = "订单id")
    private Long orderId;
    @ApiModelProperty(value = "已预选数量")
    private Integer isPreselectedNum;
    @ApiModelProperty(value = "未预选数量")
    private Integer notPreselectedNum;
}
