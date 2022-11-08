package com.mfexpress.rent.deliver.dto.data.serve;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
@ApiModel(value = "租赁服务单")
public class ServeVO {

    @ApiModelProperty(value = "订单id")
    private String orderId;
    @ApiModelProperty(value = "客户id")
    private Integer customerId;
    @ApiModelProperty(value = "提车公司")
    private String customerName;
    @ApiModelProperty(value = "租赁服务单编号")
    private String serveNo;
    @ApiModelProperty(value = "租赁方式id")
    private Integer leaseModelId;
    @ApiModelProperty(value = "租赁方式描述")
    private String leaseModelDisplay;
    @ApiModelProperty(value = "车型id")
    private Integer carModelId;
    @ApiModelProperty(value = "车辆id")
    private Integer carId;
    @ApiModelProperty(value = "提车日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date extractVehicleTime;
    @ApiModelProperty(value = "交付单编号")
    private String deliverNo;

    @ApiModelProperty(value = "替换车标识")
    private Integer replaceFlag;

    @ApiModelProperty(value = "品牌id")
    private Integer brandId;
    @ApiModelProperty(value = "品牌车型描述")
    private String brandModelDisplay;
    @ApiModelProperty(value = "车牌号")
    private String carNum;
    @ApiModelProperty(value = "车架号")
    private String frameNum;
    @ApiModelProperty(value = "发车日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date deliverVehicleTime;
    @ApiModelProperty(value = "预选状态")
    private Integer isPreselected;
    @ApiModelProperty(value = "预选状态描述")
    private String isPreselectedDisplay;
    @ApiModelProperty(value = "验车状态")
    private Integer isCheck;
    @ApiModelProperty(value = "验车状态描述")
    private String isCheckDisplay;
    @ApiModelProperty(value = "保险状态")
    private Integer isInsurance;
    @ApiModelProperty(value = "保险状态描述")
    private String isInsuranceDisplay;
    @ApiModelProperty(value = "交付状态")
    private Integer deliverStatus;
    @ApiModelProperty(value = "里程")
    private Double mileage;
    @ApiModelProperty(value = "车龄")
    private Double vehicleAge;

    @ApiModelProperty(value = "交车电子合同签署状态")
    private Integer deliverContractStatus;

    @ApiModelProperty(value = "收车电子合同签署状态")
    private Integer recoverContractStatus;

    @ApiModelProperty(value = "异常收车标志位")
    private Integer recoverAbnormalFlag;

    @ApiModelProperty(value = "所属电子合同id")
    private String elecContractId;

    //新增字段
    @ApiModelProperty(value = "月租金")
    private BigDecimal rent;

    @ApiModelProperty(value = "押金")
    private BigDecimal deposit;

    @ApiModelProperty(value = "车辆运营模式")
    private Integer vehicleBusinessMode;

    @ApiModelProperty(value = "车辆运营模式描述")
    private String vehicleBusinessModeDisplay;
    @ApiModelProperty(value = "操作按钮显示，目前只会出现在发车待投保列表中，1：显示投保申请按钮，2：显示录入保单信息按钮")
    private Integer operationButton;

    @ApiModelProperty(value = "是否发起了投保标志位，1：真，0：假")
    private Integer insureApplyFlag;

    @ApiModelProperty(value = "预选车辆时所要求的车辆保险状态，1：不限制，2：只能选择交强险在保，而商业险不在保的车辆")
    private Integer vehicleInsureRequirement;

    @ApiModelProperty(value = "合同商品id")
    private Integer contractCommodityId;

    private Integer serveStatus;

}


