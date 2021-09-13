package com.mfexpress.rent.deliver.dto.data.serve;


import com.mfexpress.rent.deliver.dto.data.ListVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel(value = "服务单发车任务列表")
@Data
public class ServeDeliverTaskListVO extends ListVO {


    @ApiModelProperty(value = "发车任务数据")
    private List<ServeDeliverTaskVO> serveDeliverTaskVOList;


}
