package com.mfexpress.rent.deliver.dto.data.serve;

import com.mfexpress.rent.deliver.dto.entity.Serve;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("ServeListQryByStatus 通过状态查询服务单")
@Data
public class PassiveRenewalServeCmd {

    /*@ApiModelProperty(value = "服务单状态", required = true)
    @NotEmpty(message = "状态不能为空")
    private List<Integer> statuses;

    @ApiModelProperty(value = "一次批量处理多少条数据", required = true)
    @NotNull(message = "跨度不能为空")
    private Integer limit;*/

    @ApiModelProperty(value = "操作人id")
    @NotNull(message = "操作人id不能为空")
    private Integer operatorId;

    @ApiModelProperty(value = "被续约的服务单", required = true)
    @NotEmpty(message = "被续约的服务单不能为空")
    private List<Serve> needPassiveRenewalServeList;

}
