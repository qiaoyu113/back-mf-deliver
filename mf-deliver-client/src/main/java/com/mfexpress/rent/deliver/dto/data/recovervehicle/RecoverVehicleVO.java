package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("收车单信息")
public class RecoverVehicleVO {

    @ApiModelProperty(value = "租赁服务单编号")
    private String serveNo;

    @ApiModelProperty(value = "交付单编号")
    private String deliverNo;

    @ApiModelProperty(value = "客户姓名")
    private String customerName;
    @ApiModelProperty(value = "合同编号")
    private String contractNo;
    @ApiModelProperty(value = "车辆id")
    private Integer carId;
    @ApiModelProperty(value = "车牌号")
    private String carNum;
    @ApiModelProperty(value = "车型id")
    private Integer carModelId;
    @ApiModelProperty(value = "客户id")
    private Integer customerId;
    @ApiModelProperty(value = "品牌id")
    private Integer brandId;
    @ApiModelProperty(value = "品牌车型描述")
    private String brandModelDisplay;
    @ApiModelProperty(value = "租赁方式id")
    private Integer leaseModelId;
    @ApiModelProperty(value = "租赁方式描述")
    private String leaseModelDisplay;
    @ApiModelProperty(value = "车架号")
    private String frameNum;
    @ApiModelProperty(value = "发车时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date deliverVehicleTime;
    @ApiModelProperty(value = "收车时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date recoverVehicleTime;
    @ApiModelProperty(value = "预计收车时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expectRecoverTime;
    @ApiModelProperty(value = "验车状态")
    private Integer isCheck;
    @ApiModelProperty(value = "保险状态")
    private Integer isInsurance;
    @ApiModelProperty(value = "处理违章状态")
    private Integer isDeduction;
    @ApiModelProperty(value = "交付状态")
    private Integer deliverStatus;
    @ApiModelProperty(value = "替换车标识")
    private Integer replaceFlag;
    @ApiModelProperty(value = "服务单状态")
    private Integer serveStatus;


    @ApiModelProperty(value = "还车人姓名")
    private String contactsName;

    @ApiModelProperty(value = "还车人手机号")
    private String contactsPhone;

    @ApiModelProperty(value = "还车人身份证号")
    private String contactsCard;

    @ApiModelProperty(value = "车损费")
    private Double damageFee;

    @ApiModelProperty(value = "路边停车费")
    private Double parkFee;

    @ApiModelProperty(value = "车辆停放地id")
    private Integer wareHouseId;

    @ApiModelProperty(value = "车辆停放地名称")
    private String wareHouseDisplay;

    @ApiModelProperty(value = "合照")
    private String imgUrl;

    @ApiModelProperty(value = "电子合同id")
    private String elecContractId;

    @ApiModelProperty(value = "电子合同状态，1:生成中，2:已生成/签署中，3:已完成，4:失败")
    private Integer elecContractStatus;

    @ApiModelProperty(value = "电子合同失败原因，1：作废，2：过期，3其他")
    private Integer elecContractFailureReason;

    @ApiModelProperty(value = "收车类型描述，正常收车或异常收车")
    private String recoverTypeDisplay;

    @ApiModelProperty(value = "异常收车标志位")
    private Integer recoverAbnormalFlag;

    @ApiModelProperty(value = "收车电子合同签署状态")
    private Integer recoverContractStatus;

    @ApiModelProperty(value = "租赁服务单所属城市id")
    private Integer cityId;

    private Integer orgId;

}
