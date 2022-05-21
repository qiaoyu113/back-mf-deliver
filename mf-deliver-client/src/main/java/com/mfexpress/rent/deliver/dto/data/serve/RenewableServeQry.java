package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("ServeListByCustomerQry 通过客户相关信息查询其下可续约的服务单命令")
@Data
public class RenewableServeQry {

    @ApiModelProperty(value = "客户id", required = true)
    @NotNull(message = "客户id不能为空")
    private Integer customerId;

    @ApiModelProperty(value = "车辆id")
    private Integer carId;

    @ApiModelProperty(value = "OA合同编号")
    private String oaContractCode;

    @ApiModelProperty(value = "租赁服务单状态，2：已发车；5：维修中")
    private Integer status;

    @ApiModelProperty(value = "租赁方式id")
    private List<Integer> leaseMode;

}
