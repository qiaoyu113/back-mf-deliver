package com.mfexpress.rent.deliver.dto.es;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mfexpress.component.starter.elasticsearch.mapper.annotations.Document;
import com.mfexpress.component.starter.elasticsearch.mapper.annotations.enums.StringType;
import com.mfexpress.component.starter.elasticsearch.mapper.annotations.fieldtype.MultiField;
import com.mfexpress.component.starter.elasticsearch.mapper.annotations.fieldtype.MultiNestedField;
import com.mfexpress.component.starter.elasticsearch.mapper.annotations.fieldtype.StringField;
import com.mfexpress.component.starter.elasticsearch.mapper.annotations.meta.MetaField_All;
import com.mfexpress.rent.deliver.constant.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel(value = "交付单es对象")
@Data
@Document(_type = Constants.ES_DELIVER_TYPE, _all = @MetaField_All(enabled = false))
public class DeliverES {

    private String serveNo;

    private String deliverNo;

    @ApiModelProperty(value = "服务单状态")
    private Integer serveStatus;

    @ApiModelProperty(value = "交付单状态")
    private Integer deliverStatus;

    @ApiModelProperty(value = "排序规则")
    private Integer sort;

    @ApiModelProperty(value = "验车状态")
    private Integer isCheck;

    @ApiModelProperty(value = "收车电子合同签署状态")
    private Integer recoverContractStatus;

    @ApiModelProperty(value = "保险状态")
    private Integer isInsurance;

    @ApiModelProperty(value = "处理违章状态")
    private Integer isDeduction;

    @ApiModelProperty(value = "异常收车标志位")
    private Integer recoverAbnormalFlag;

    // ------------筛选条件start
    @ApiModelProperty(value = "客户id")
    private Integer customerId;

    @ApiModelProperty(value = "客户手机号")
    private String customerPhone;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    /*@ApiModelProperty(value = "品牌id")
    private Integer brandId;*/

    @ApiModelProperty(value = "车辆id")
    private Integer carId;

    @ApiModelProperty(value = "车牌号")
    private String carNum;

    @ApiModelProperty(value = "车型id")
    private Integer carModelId;

    @ApiModelProperty(value = "收车单预计还车日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expectRecoverTime;

    @ApiModelProperty(value = "发车日期/交车日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date deliverVehicleTime;

    // ------------筛选条件end

    @ApiModelProperty(value = "品牌车型描述")
    private String brandModelDisplay;

    @ApiModelProperty(value = "合同编号")
    private String contractNo;

    @ApiModelProperty(value = "车架号")
    private String frameNum;

    @ApiModelProperty(value = "收车类型描述，正常收车或异常收车")
    private String recoverTypeDisplay;

    @ApiModelProperty(value = "收车时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date recoverVehicleTime;

    private Date updateTime;

    private Integer orgId;

}
