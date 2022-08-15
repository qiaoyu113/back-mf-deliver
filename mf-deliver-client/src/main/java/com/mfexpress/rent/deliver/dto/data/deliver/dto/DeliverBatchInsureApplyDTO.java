package com.mfexpress.rent.deliver.dto.data.deliver.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "后市场端返回的发车批量投保申请DTO")
public class DeliverBatchInsureApplyDTO {

    @ApiModelProperty(value = "交强险批量受理编号")
    private String compulsoryBatchAcceptCode;

    @ApiModelProperty(value = "商业险批量受理编号")
    private String commercialBatchAcceptCode;

    @ApiModelProperty(value = "投保返回结果")
    private List<DeliverInsureApplyDTO> deliverInsureApplyDTOS;

}


