package com.mfexpress.rent.deliver.dto.data.deliver.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@ApiModel(value = "创建保单DTO")
@Data
public class CreateInsurancePolicyCmd {

    @ApiModelProperty(value = "投保来源")
    private Integer insureSource = 2;

    @ApiModelProperty(value = "投保业务类型")
    private Integer insureBusinessType = 2;

    @ApiModelProperty(value = "投保开始日期")
    private Date startInsureDate;

    @ApiModelProperty(value = "投保结束日期")
    private Date endInsureDate;

    @ApiModelProperty(value = "承保公司id")
    private Integer insuranceCompanyId;

    @ApiModelProperty(value = "投保人")
    private String policyHolder;

    @ApiModelProperty(value = "被保人")
    private String insuredPerson;

    @ApiModelProperty(value = "保单号")
    private String policyNo;

    @ApiModelProperty(value = "保费")
    private String premium = "0.0";

    @ApiModelProperty(value = "座位险保额（万元）")
    private String seatInsuredAmount = "0.0";

    @ApiModelProperty(value = "三者险保额（万元）")
    private String thirdInsuredAmount = "0.0";

    @ApiModelProperty(value = "车辆id")
    private Integer vehicleId;

    @ApiModelProperty(value = "保单凭证")
    private List<String> policyVouchers;

    @ApiModelProperty(value = "操作人")
    private Integer operatorUserId;

    @ApiModelProperty(value = "备注")
    private String remarks;

}
