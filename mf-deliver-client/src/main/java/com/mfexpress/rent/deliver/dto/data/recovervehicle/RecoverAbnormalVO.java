package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

@ApiModel("异常收车信息VO")
@Data
public class RecoverAbnormalVO {

    @ApiModelProperty(value = "上报人姓名")
    private String reporterName;

    @ApiModelProperty(value = "上报人手机号")
    private String reporterPhone;

    @ApiModelProperty(value = "原因")
    @NotEmpty(message = "原因不能为空")
    private String reason;

    @ApiModelProperty(value = "图片链接")
    private List<String> imgUrls;

    @ApiModelProperty(value = "收车时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date recoverTime;

}
