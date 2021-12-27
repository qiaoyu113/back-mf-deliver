package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@ApiModel(value = "ElecContractOperationRecordVO 电子交接合同操作记录VO")
@Data
@Builder
public class ElecContractOperationRecordVO {

    @ApiModelProperty(value = "操作类型")
    private Integer operationType;

    @ApiModelProperty(value = "操作类型含义")
    private String operationTypeDisplay;

    @ApiModelProperty(value = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date operationTime;

}
