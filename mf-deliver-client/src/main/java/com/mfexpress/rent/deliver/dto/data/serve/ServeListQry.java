package com.mfexpress.rent.deliver.dto.data.serve;

import com.mfexpress.rent.deliver.dto.data.ListQry;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@ApiModel(value = "交付信息列表")
@Data
@EqualsAndHashCode(callSuper = true)
public class ServeListQry extends ListQry {

    @ApiModelProperty(value = "服务单状态")
    private List<Integer> statuses;

}
