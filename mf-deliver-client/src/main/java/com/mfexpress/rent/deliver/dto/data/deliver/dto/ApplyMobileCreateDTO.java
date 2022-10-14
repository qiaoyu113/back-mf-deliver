package com.mfexpress.rent.deliver.dto.data.deliver.dto;

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
@ApiModel(value = "H5创建商业险申请结果DTO")
public class ApplyMobileCreateDTO {

    @ApiModelProperty(value = "车辆ID")
    private Long vehicleId;

    @ApiModelProperty(value = "申请编码")
    private String applyCode;

    @ApiModelProperty(value = "申请ID")
    private Long applyId;

    @ApiModelProperty(value = "创建状态：1-成功；0-失败")
    private Integer createStatus;

    @ApiModelProperty(value = "失败原因")
    private String failReason;
}
