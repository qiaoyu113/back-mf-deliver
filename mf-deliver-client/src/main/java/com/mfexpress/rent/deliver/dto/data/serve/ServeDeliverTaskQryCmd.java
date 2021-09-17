package com.mfexpress.rent.deliver.dto.data.serve;


import com.mfexpress.rent.deliver.dto.data.ListQry;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("发车任务查询")
public class ServeDeliverTaskQryCmd extends ListQry {

    @ApiModelProperty(value = "客户名称")
    private String keyword;


    @ApiModelProperty(value = "查询tag", example = "1：查询待发车，2：查询已完成")
    private Integer tag;
}
