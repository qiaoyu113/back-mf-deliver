package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ServeFastPreselectedVO {

    @ApiModelProperty(value = "车型id")
    private Integer carModelId;
    @ApiModelProperty(value = "品牌id")
    private Integer brandId;

    @ApiModelProperty(value = "客户id")
    private Integer customerId;

    @ApiModelProperty(value = "品牌车型描述")
    private String brandModelDisplay;
    @ApiModelProperty(value = "数量")
    private Integer num;
    @ApiModelProperty(value = "服务单数据")
    private List<ServeVO> serveVOList;
}
