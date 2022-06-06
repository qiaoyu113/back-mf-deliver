package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "合同签署自动完成命令")
public class AutoCompletedCmd {

    private Integer customerId;

    private Integer deliverType;

    private String deliverNo;

    private Integer carId;

    private Integer deliverStatus;

    private String serveNo;
}
