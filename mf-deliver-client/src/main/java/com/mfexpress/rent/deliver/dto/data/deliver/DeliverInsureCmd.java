package com.mfexpress.rent.deliver.dto.data.deliver;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DeliverInsureCmd {

    @ApiModelProperty(value = "租赁服务单编号列表")
    private List<String> serveNoList;

    @ApiModelProperty(value = "车辆id列表")
    private List<Integer> carIdList;

    @ApiModelProperty(value = "开始投保日期")
    @JsonFormat(timezone = "GMT+8")
    private Date startInsureDate;
    @ApiModelProperty(value = "结束投保如期")
    @JsonFormat(timezone = "GMT+8")
    private Date endInsureDate;

    private Integer carServiceId;


}
