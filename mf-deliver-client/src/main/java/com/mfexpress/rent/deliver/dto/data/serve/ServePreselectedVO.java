package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("发车任务待预选数据")
public class ServePreselectedVO {


    @ApiModelProperty(value = "数量")
    private Integer num;
    @ApiModelProperty(value = "车型id")
    private Integer carModelId;
    @ApiModelProperty(value = "车辆id")
    private Integer carId;
    @ApiModelProperty(value = "品牌id")
    private Integer brandId;
    @ApiModelProperty(value = "品牌车型描述")
    private String brandModelDisplay;

    @ApiModelProperty(value = "租赁服务单列表")
    private List<ServeVO> serveVOList;
}
