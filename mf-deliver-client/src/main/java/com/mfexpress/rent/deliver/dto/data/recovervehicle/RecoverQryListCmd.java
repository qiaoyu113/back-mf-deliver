package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import com.mfexpress.rent.deliver.dto.data.ListQry;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("收车需求列表查询查询")
public class RecoverQryListCmd extends ListQry {


    @ApiModelProperty(value = "客户id")
    private String keyword;

    @ApiModelProperty(value = "车型id")
    private Integer carModelId;

    @ApiModelProperty(value = "品牌id")
    private Integer brandId;

    @ApiModelProperty(value = "预计收车日期start")
    private Date expectRecoverStartTime;
    @ApiModelProperty(value = "预计收车日期end")
    private Date expectRecoverEndTime;
    @ApiModelProperty(value = "列表tag 申请收车列表1全部 2待收车 3已完成；收车任务列表 4全部 5待验车 6待退保 7待处理违章 8已完成；契约锁流程新增的状态，20:代收，21:待签")
    private Integer tag;
    @ApiModelProperty(value = "发车日期时间段start")
    private Date startDeliverTime;
    @ApiModelProperty(value = "发车日期时间段end")
    private Date endDeliverTime;


}
