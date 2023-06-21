package com.mfexpress.rent.deliver.dto.data.deliver.cmd;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "发车时由客户投保命令")
public class DeliverInsureByCustomerCmd extends BaseCmd {

    @ApiModelProperty(value = "租赁服务单号", required = true)
    @NotBlank(message = "租赁服务单号不能为空")
    private String serveNo;

    @ApiModelProperty(value = "交付单号")
    private String deliverNo;

    @ApiModelProperty(value = "开始投保日期", required = true)
    @NotNull(message = "开始投保日期不能为空")
    private Date startInsureDate;

    @ApiModelProperty(value = "结束投保如期", required = true)
    @NotNull(message = "结束投保如期不能为空")
    private Date endInsureDate;

    @ApiModelProperty(value = "保单号", required = true)
    // 可输入内容为25位数字、字母、符号
    @NotBlank(message = "保单号不能为空")
    private String policyNo;

    @ApiModelProperty(value = "承保公司", required = true)
    @NotNull(message = "承保公司不能为空")
    private Integer acceptCompanyId;

    @ApiModelProperty(value = "投保公司", required = true)
    @NotBlank(message = "投保公司不能为空")
    private String insureCompany;

    @ApiModelProperty(value = "保单文件", required = true)
    @NotEmpty(message = "保单文件不能为空")
    private List<String> fileUrls;

    @ApiModelProperty(value = "商业险保单id")
    private String commercialPolicyId;

    @ApiModelProperty(value = "交强险状态是否生效中 true-生效中 false-非生效中")
    private Boolean compulsoryStatus;

}
