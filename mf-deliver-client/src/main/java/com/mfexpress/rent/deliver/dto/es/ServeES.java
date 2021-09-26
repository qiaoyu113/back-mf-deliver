package com.mfexpress.rent.deliver.dto.es;

import com.mfexpress.rent.deliver.dto.data.OrderCarModelVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@ApiModel(value = "交付es数据")
@Data
public class ServeES {

    @ApiModelProperty(value = "订单id")
    private String orderId;
    @ApiModelProperty(value = "客户名称")
    private String customerName;
    @ApiModelProperty(value = "客户手机号")
    private String customerPhone;
    @ApiModelProperty(value = "客户id")
    private Integer customerId;
    @ApiModelProperty(value = "合同编号")
    private String contractNo;
    @ApiModelProperty(value = "提车日期")
    private Date extractVehicleTime;
    @ApiModelProperty(value = "订单车型列表")
    private List<OrderCarModelVO> carModelVOList;


    @ApiModelProperty(value = "租赁服务单编号")
    private String serveNo;
    @ApiModelProperty(value = "租赁方式id")
    private Integer leaseModelId;
    @ApiModelProperty(value = "租赁方式描述")
    private String leaseModelDisplay;
    @ApiModelProperty(value = "交付单编号")
    private String deliverNo;
    @ApiModelProperty(value = "车辆id")
    private Integer carId;

    @ApiModelProperty(value = "品牌id")
    private Integer brandId;
    @ApiModelProperty(value = "车型id")
    private Integer carModelId;
    @ApiModelProperty(value = "品牌车型描述")
    private String brandModelDisplay;
    @ApiModelProperty(value = "车牌号")
    private String carNum;
    @ApiModelProperty(value = "车架号")
    private String frameNum;
    @ApiModelProperty(value = "发车日期")
    private Date deliverVehicleTime;
    @ApiModelProperty(value = "收车日期")
    private Date recoverVehicleTime;
    @ApiModelProperty(value = "排序规则")
    private Integer sort;
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
    @ApiModelProperty(value = "预计还车日期")
    private Date expectRecoverTime;

    private Integer cityId;

    private Integer orgId;

    private Integer saleId;
    private Integer carServiceId;
    private Date updateTime;


}
