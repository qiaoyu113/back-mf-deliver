package com.mfexpress.rent.deliver.dto.data.recovervehicle;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel(value = "申请收车信息列表")
public class RecoverApplyListCmd {

    @ApiModelProperty("预计收车日期")
    @JsonFormat(timezone = "GMT+8")
    private Date expectRecoverTime;

    @ApiModelProperty("收车信息")
    private List<RecoverApplyCmd> recoverApplyCmdList;


}
