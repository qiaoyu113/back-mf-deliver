package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "服务单租赁日报")
public class ServeDailyDTO {

    private String serveNo;

    private Integer customerId;


}
