package com.mfexpress.rent.deliver.dto.es;

import com.mfexpress.component.starter.elasticsearch.mapping.mapper.annotations.Document;
import com.mfexpress.component.starter.elasticsearch.mapping.mapper.annotations.enums.StringType;
import com.mfexpress.component.starter.elasticsearch.mapping.mapper.annotations.fieldtype.MultiField;
import com.mfexpress.component.starter.elasticsearch.mapping.mapper.annotations.fieldtype.MultiNestedField;
import com.mfexpress.component.starter.elasticsearch.mapping.mapper.annotations.fieldtype.StringField;
import com.mfexpress.component.starter.elasticsearch.mapping.mapper.annotations.meta.MetaField_All;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.dto.data.OrderCarModelVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@ApiModel(value = "服务单es对象")
@Data
@Document(_type = Constants.ES_SERVE_TYPE, _all = @MetaField_All(enabled = false))
public class ServeES {

    @ApiModelProperty(value = "订单id")
    @MultiField(
            mainField = @StringField(type = StringType.Text),
            fields = {
                    @MultiNestedField(name = "keyword", field = @StringField(type = StringType.Keyword, ignore_above = 256))
            }
    )
    private String orderId;

    @ApiModelProperty(value = "客户名称")
    @MultiField(
            mainField = @StringField(type = StringType.Text),
            fields = {
                    @MultiNestedField(name = "keyword", field = @StringField(type = StringType.Keyword, ignore_above = 256))
            }
    )
    private String customerName;

    @ApiModelProperty(value = "客户手机号")
    @MultiField(
            mainField = @StringField(type = StringType.Text),
            fields = {
                    @MultiNestedField(name = "keyword", field = @StringField(type = StringType.Keyword, ignore_above = 256))
            }
    )
    private String customerPhone;

    @ApiModelProperty(value = "客户id")
    private Integer customerId;

    @ApiModelProperty(value = "合同编号")
    @MultiField(
            mainField = @StringField(type = StringType.Text),
            fields = {
                    @MultiNestedField(name = "keyword", field = @StringField(type = StringType.Keyword, ignore_above = 256))
            }
    )
    private String contractNo;

    @ApiModelProperty(value = "提车日期")
    private Date extractVehicleTime;

    @ApiModelProperty(value = "订单车型列表")
    private List<OrderCarModelVO> carModelVOList;


    @ApiModelProperty(value = "租赁服务单编号")
    @MultiField(
            mainField = @StringField(type = StringType.Text),
            fields = {
                    @MultiNestedField(name = "keyword", field = @StringField(type = StringType.Keyword, ignore_above = 256))
            }
    )
    private String serveNo;

    @ApiModelProperty(value = "租赁方式id")
    private Integer leaseModelId;

    @ApiModelProperty(value = "租赁方式描述")
    @MultiField(
            mainField = @StringField(type = StringType.Text),
            fields = {
                    @MultiNestedField(name = "keyword", field = @StringField(type = StringType.Keyword, ignore_above = 256))
            }
    )
    private String leaseModelDisplay;

    @ApiModelProperty(value = "交付单编号")
    @MultiField(
            mainField = @StringField(type = StringType.Text),
            fields = {
                    @MultiNestedField(name = "keyword", field = @StringField(type = StringType.Keyword, ignore_above = 256))
            }
    )
    private String deliverNo;

    @ApiModelProperty(value = "车辆id")
    private Integer carId;

    @ApiModelProperty(value = "品牌id")
    private Integer brandId;

    @ApiModelProperty(value = "车型id")
    private Integer carModelId;

    @ApiModelProperty(value = "品牌车型描述")
    @MultiField(
            mainField = @StringField(type = StringType.Text),
            fields = {
                    @MultiNestedField(name = "keyword", field = @StringField(type = StringType.Keyword, ignore_above = 256))
            }
    )
    private String brandModelDisplay;

    @ApiModelProperty(value = "车牌号")
    @MultiField(
            mainField = @StringField(type = StringType.Text),
            fields = {
                    @MultiNestedField(name = "keyword", field = @StringField(type = StringType.Keyword, ignore_above = 256))
            }
    )
    private String carNum;

    @ApiModelProperty(value = "车架号")
    @MultiField(
            mainField = @StringField(type = StringType.Text),
            fields = {
                    @MultiNestedField(name = "keyword", field = @StringField(type = StringType.Keyword, ignore_above = 256))
            }
    )
    private String frameNum;

    @ApiModelProperty(value = "发车日期")
    private Date deliverVehicleTime;

    @ApiModelProperty(value = "收车日期")
    private Date recoverVehicleTime;

    @ApiModelProperty(value = "h5排序规则")
    private Integer sort;

    @ApiModelProperty(value = "服务单状态排序规则")
    private Integer serveStatusSort;

    @ApiModelProperty(value = "预选状态")
    private Integer isPreselected;

    @ApiModelProperty(value = "验车状态")
    private Integer isCheck;

    @ApiModelProperty(value = "保险状态")
    private Integer isInsurance;

    @ApiModelProperty(value = "交付状态")
    private Integer deliverStatus;

    @ApiModelProperty(value = "服务单状态")
    private Integer serveStatus;

    @ApiModelProperty(value = "里程")
    private Double mileage;

    @ApiModelProperty(value = "车龄")
    private Double vehicleAge;

    @ApiModelProperty(value = "处理违章状态")
    private Integer isDeduction;

    @ApiModelProperty(value = "收车单预计还车日期")
    private Date expectRecoverTime;

    @ApiModelProperty(value = "租赁结束日期")
    private Date leaseEndDate;

    @ApiModelProperty(value = "根据发车日期和租赁期限计算出来的预计收车日期")
    private Date expectRecoverDate;

    @ApiModelProperty(value = "替换车标识")
    private Integer replaceFlag;

    @ApiModelProperty(value = "订单商品id")
    private Integer goodsId;

    @ApiModelProperty(value = "合同商品id")
    private Integer contractCommodityId;

    @ApiModelProperty(value = "租金")
    @MultiField(
            mainField = @StringField(type = StringType.Text),
            fields = {
                    @MultiNestedField(name = "keyword", field = @StringField(type = StringType.Keyword, ignore_above = 256))
            }
    )
    private String rent;

    @ApiModelProperty(value = "租金比例")
    private String rentRatio;

    @ApiModelProperty(value = "押金")
    @MultiField(
            mainField = @StringField(type = StringType.Text),
            fields = {
                    @MultiNestedField(name = "keyword", field = @StringField(type = StringType.Keyword, ignore_above = 256))
            }
    )
    private String deposit;

    private Integer cityId;

    private Integer orgId;

    private Integer saleId;

    private Integer carServiceId;

    private Date updateTime;

    @ApiModelProperty(value = "交车电子合同签署状态")
    private Integer deliverContractStatus;

    @ApiModelProperty(value = "收车电子合同签署状态")
    private Integer recoverContractStatus;

    @ApiModelProperty(value = "异常收车标志位")
    private Integer recoverAbnormalFlag;

    @ApiModelProperty(value = "续约状态，0：未续约，1：主动续约，2：被动/自动续约")
    private Integer renewalType;

    @ApiModelProperty(value = "收车类型描述，正常收车或异常收车")
    private String recoverTypeDisplay;

}
