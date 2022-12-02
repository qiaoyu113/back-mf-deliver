package com.mfexpress.rent.deliver.dto.data.serve.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("将过期合同信息")
public class ContractWillExpireInfoDTO {

    @ApiModelProperty("管理区id")
    private Integer orgId;

    @ApiModelProperty("交付单id")
    private Long deliverId;
    @ApiModelProperty("交付单编号")
    private String deliverNo;
    @ApiModelProperty("车辆id")
    private Integer carId;
    @ApiModelProperty("车牌号")
    private String carNum;


    @ApiModelProperty("订单id")
    private Long orderId;
    @ApiModelProperty("服务单id")
    private Long serveId;
    @ApiModelProperty("服务单编号")
    private String serveNo;
    @ApiModelProperty("客户id")
    private Integer customerId;
    @ApiModelProperty("状态")
    private Integer status;
    @ApiModelProperty("oa合同编号")
    private String oaContractCode;
    @ApiModelProperty("预计收车日期")
    private String expectRecoverDate;


}
