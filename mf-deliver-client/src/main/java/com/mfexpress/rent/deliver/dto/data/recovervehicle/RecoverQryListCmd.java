package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import com.mfexpress.rent.deliver.dto.data.ListQry;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("收车需求列表查询查询")
public class RecoverQryListCmd extends ListQry {

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "客户手机号")
    private String customerPhone;

    @ApiModelProperty(value = "车型id")
    private Integer carModelId;

    @ApiModelProperty(value = "品牌id")
    private Integer brandId;

    @ApiModelProperty(value = "预计收车日期")
    private Date expectRecoverTime;

    @ApiModelProperty(value = "排序规则", example = "0:其它，1:收车申请全部tab,2:收车任务全部tab")
    private Integer sortTag;

    @ApiModelProperty(value = "发车日期时间段start")
    private Date startDeliverTime;
    @ApiModelProperty(value = "发车日期时间段end")
    private Date endDeliverTime;
}
