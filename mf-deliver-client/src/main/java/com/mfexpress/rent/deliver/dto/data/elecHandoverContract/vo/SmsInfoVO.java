package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "电子交接合同短信信息VO")
@Data
public class SmsInfoVO {

    @ApiModelProperty(value = "是否可重发短信，1：是，0：否")
    private Integer sendSmsFlag;

    @ApiModelProperty(value = "可重发短信的情况下，短信还剩多少秒可发送")
    private Integer smsCountDown;

}
