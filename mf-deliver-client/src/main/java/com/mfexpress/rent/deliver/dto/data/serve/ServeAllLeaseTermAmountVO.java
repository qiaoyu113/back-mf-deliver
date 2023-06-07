package com.mfexpress.rent.deliver.dto.data.serve;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@ApiModel("ServeAllLeaseTermAmountVO 服务单信息和在其租赁周期中产生的计费信息聚合而成的对象")
@Data
public class ServeAllLeaseTermAmountVO {

    @ApiModelProperty(value = "租赁服务单编号")
    private String serveNo;

    @ApiModelProperty(value = "车辆id")
    private Integer carId;

    @ApiModelProperty(value = "车牌号")
    private String plateNumber;

    @ApiModelProperty(value = "车辆运营模式")
    private Integer vehicleBusinessMode;

    @ApiModelProperty(value = "车辆运营模式描述")
    private String vehicleBusinessModeDisplay;

    @ApiModelProperty(value = "车型id")
    private Integer carModelId;

    @ApiModelProperty(value = "车型含义")
    private String carModelDisplay;

    @ApiModelProperty(value = "租赁价格")
    private String rentFee;

    @ApiModelProperty(value = "服务费")
    private String serviceFee;

    @ApiModelProperty(value = "月租金 = 租赁价格+服务费")
    private String rent;

    @ApiModelProperty(value = "押金")
    private String deposit;

    @ApiModelProperty(value = "总欠费")
    private String totalArrears;

    @ApiModelProperty(value = "租赁方式id")
    private Integer leaseModelId;

    @ApiModelProperty(value = "租赁方式id含义")
    private String leaseModelDisplay;

    @ApiModelProperty(value = "服务单状态")
    private Integer serveStatus;

    @ApiModelProperty(value = "服务单状态含义")
    private String serveStatusDisplay;

    @ApiModelProperty(value = "客户id")
    private Integer customerId;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "客户id组织销售人员")
    private String customerIDCardOrgSaleName;

    @ApiModelProperty(value = "oa合同编号")
    private String oaContractCode;

    @ApiModelProperty(value = "所属大区id")
    private Integer orgId;

    @ApiModelProperty(value = "所属大区名称")
    private String orgName;

    @ApiModelProperty(value = "收车时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date recoverVehicleTime;

    @ApiModelProperty(value = "预计收车时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expectRecoverDate;

    private String expectRecoverDateChar;

    @ApiModelProperty(value = "是否允许重新激活，1：真，0：假")
    private Integer enableReactivate;

    @ApiModelProperty(value = "合同商品id")
    private Integer contractCommodityId;

    @ApiModelProperty(value = "实缴押金")
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal actualDeposit;

    @ApiModelProperty(value = "首次发车日期")
    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN, timezone = "GMT+8")
    private Date firstIssueDate;

    private String firstIssueDateChar;

    @ApiModelProperty(value = "最近发车日期")
    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN, timezone = "GMT+8")
    private Date recentlyIssueDate;

    private String recentlyIssueDateChar;

    @ApiModelProperty(value = "最近收车日期")
    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN, timezone = "GMT+8")
    private Date recentlyRecoverDate;

    private String recentlyRecoverDateChar;

    @ApiModelProperty(value = "替换车标识 1是替换车,0不是")
    private Integer replaceFlag;

    @ApiModelProperty(value = "客户类别")
    private Integer customerCategory;

    @ApiModelProperty(value = "客户类别含义")
    private String customerCategoryDisplay;

    @ApiModelProperty(value = "历史租赁车辆id")
    private List<Integer> historyVehicleIds;

    @ApiModelProperty(value = "历史租赁车辆车牌号")
    private List<String> historyVehiclePlate;

    @ApiModelProperty(value = "费用业务类型")
    private Integer businessType;

    @ApiModelProperty(value = "费用业务类型含义")
    private String businessTypeDisplay;

    @ApiModelProperty(value = "首次发车日期")
    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN, timezone = "GMT+8")
    private Date firstDeliverVehicleDate;

    @ApiModelProperty(value = "最近收车日期")
    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN, timezone = "GMT+8")
    private Date recentlyRecoverVehicleDate;

    @ApiModelProperty(value = "销售人员id")
    private Integer saleId;

    @ApiModelProperty(value = "销售人员名称")
    private String salesPersonName;

    @ApiModelProperty(value = "签约类型")
    private Integer signedType;

    @ApiModelProperty(value = "签约类型含义")
    private String signedTypeDisplay;

    @ApiModelProperty(value = "租赁天数")
    private Integer leaseDays;

    @ApiModelProperty(value = "租赁月数")
    private Integer leaseMonths;

    @ApiModelProperty(value = "租赁期限含义")
    private String leaseTermDisplay;

}
