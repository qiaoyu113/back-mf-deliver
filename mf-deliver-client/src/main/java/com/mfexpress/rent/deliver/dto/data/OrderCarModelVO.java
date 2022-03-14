package com.mfexpress.rent.deliver.dto.data;


import com.mfexpress.component.starter.elasticsearch.mapper.annotations.enums.StringType;
import com.mfexpress.component.starter.elasticsearch.mapper.annotations.fieldtype.MultiField;
import com.mfexpress.component.starter.elasticsearch.mapper.annotations.fieldtype.MultiNestedField;
import com.mfexpress.component.starter.elasticsearch.mapper.annotations.fieldtype.StringField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "订单车型列表")
@Data
public class OrderCarModelVO {

    @ApiModelProperty(value = "车型id")
    private Integer carModelId;

    @ApiModelProperty(value = "品牌id")
    private Integer brandId;
    @ApiModelProperty(value = "品牌车型描述")
    private String brandModelDisplay;
    @ApiModelProperty(value = "数量")
    private Integer num;
}
