package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("订单生成服务单")
public class ServeAddDTO {

    @ApiModelProperty(value = "订单id")
    private Long orderId;
    @ApiModelProperty(value = "客户id")
    private Integer customerId;
    @ApiModelProperty(value = "车辆信息")
    private List<ServeVehicleDTO> vehicleDTOList;

    @ApiModelProperty(value = "城市id")
    private Integer cityId;

    @ApiModelProperty(value = "创建人id")
    private Integer createId;

    @ApiModelProperty(value = "组织id")
    private Integer orgId;
    @ApiModelProperty(value = "销售id")
    private Integer saleId;
    @ApiModelProperty(value = "业务类型")
    private Integer businessType;

}
