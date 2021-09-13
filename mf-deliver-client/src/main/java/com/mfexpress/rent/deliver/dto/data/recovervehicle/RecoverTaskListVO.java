package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import com.mfexpress.rent.deliver.dto.data.ListVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("收车任务列表")
public class RecoverTaskListVO extends ListVO {


    @ApiModelProperty(value = "收车单信息")
    private List<RecoverVehicleVO> recoverVehicleVOList;


}
