package com.mfexpress.rent.deliver.dto.data.serve.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mfexpress.rent.deliver.constant.ReplaceVehicleDepositPayTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "服务单调整记录VO")
public class ServeAdjustRecordVo {

    @ApiModelProperty(value = "服务单编号", required = true)
    private String serveNo;

    /**
     * 变更后租赁方式：1、正常租赁
     */
    @ApiModelProperty(value = "变更后租赁方式：1、正常租赁", required = true)
    private Integer chargeLeaseModelId;

    @ApiModelProperty(value = "变更后租赁方式Label")
    private String chargeLeaseModel;

    /**
     * 变更后租金
     */
    @ApiModelProperty(value = "变更后租金", required = true)
    private BigDecimal chargeRentAmount;

    /**
     * 变更后押金
     */
    @ApiModelProperty(value = "变更后押金", required = true)
    private BigDecimal chargeDepositAmount;

    /**
     * 预计收车日期
     */
    @ApiModelProperty(value = "预计收车日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expectRecoverTime;

    /**
     * 押金支付方式：1、押金账本支付;2、原车押金
     */
    @ApiModelProperty(value = "押金支付方式：1、押金账本支付;2、原车押金", required = true)
    private Integer depositPayType;


    @ApiModelProperty(value = "押金支付方式TITLE")
    public String getDepositPayTypeTitle() {
        if (getDepositPayType() != null) {
            return ReplaceVehicleDepositPayTypeEnum.getTitle(this.depositPayType);
        }

        return "";
    }
}
