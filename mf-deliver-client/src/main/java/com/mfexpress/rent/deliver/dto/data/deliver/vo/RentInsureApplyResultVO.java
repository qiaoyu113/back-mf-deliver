package com.mfexpress.rent.deliver.dto.data.deliver.vo;

import com.mfexpress.rent.deliver.dto.data.deliver.dto.ApplyMobileCreateDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel(value = "保单申请结果VO")
@Data
public class RentInsureApplyResultVO {

    @ApiModelProperty(value = "交强险受理编号")
    private String compulsoryBatchAcceptCode;

    @ApiModelProperty(value = "商业险受理编号")
    private String commercialBatchAcceptCode;

    @ApiModelProperty(value = "商业险申请返回结果")
    private List<ApplyMobileCreateDTO> commercialApplyList;

    @ApiModelProperty(value = "交强险申请返回结果")
    private List<ApplyMobileCreateDTO> compulsoryApplyList;

}
