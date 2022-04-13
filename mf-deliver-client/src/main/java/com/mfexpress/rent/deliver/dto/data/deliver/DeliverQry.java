package com.mfexpress.rent.deliver.dto.data.deliver;

import com.mfexpress.rent.deliver.dto.data.ListQry;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel(value = "交付单查询对象")
@Data
public class DeliverQry extends ListQry {

    @ApiModelProperty(value = "交付单状态")
    private List<Integer> deliverStatus;

    @ApiModelProperty(value = "交付单有效状态")
    private List<Integer> status;

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;
}
