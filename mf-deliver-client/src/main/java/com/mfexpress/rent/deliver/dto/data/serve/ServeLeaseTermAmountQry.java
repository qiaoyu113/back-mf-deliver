package com.mfexpress.rent.deliver.dto.data.serve;

import com.mfexpress.rent.deliver.dto.data.ListQry;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@ApiModel("ServeLeaseTermAmountQry 查询服务单信息和其租赁周期中产生的计费信息命令")
@Data
@EqualsAndHashCode(callSuper = true)
public class ServeLeaseTermAmountQry extends ListQry {

    @ApiModelProperty(value = "客户id")
    private Integer customerId;

    @ApiModelProperty(value = "车辆id")
    private Integer carId;

    @ApiModelProperty(value = "车型id")
    private Integer carModelId;

    @ApiModelProperty(value = "大区id")
    private Integer orgId;

    @ApiModelProperty(value = "租赁方式id")
    private Integer leaseModelId;

    @ApiModelProperty(value = "oa合同编号")
    private String oaContractNo;

    @ApiModelProperty(value = "租赁服务单状态")
    private Integer serveStatus;

    private Integer userOfficeId;

    @ApiModelProperty(value = "车辆运营模式")
    private Integer vehicleBusinessMode;

    @ApiModelProperty(value = "预计收车日期开始")
    private Date expectRecoverDateStart;

    @ApiModelProperty(value = "预计收车日期结束")
    private Date expectRecoverDateEnd;

    @ApiModelProperty(value = "服务单号")
    private String serveNo;

}
