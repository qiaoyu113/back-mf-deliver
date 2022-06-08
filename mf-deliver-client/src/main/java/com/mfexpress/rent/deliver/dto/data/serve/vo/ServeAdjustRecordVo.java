package com.mfexpress.rent.deliver.dto.data.serve.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@ApiModel(value = "服务单调整记录VO")
public class ServeAdjustRecordVo {

    @ApiModelProperty(value = "服务单编号")
    private String serveNo;

    /**
     * 变更后租赁方式：1、正常租赁
     */
    @ApiModelProperty(value = "变更后租赁方式：1、正常租赁")
    private Integer chargeLeaseModelId;

    @ApiModelProperty(value = "变更后租赁方式Label")
    private String chargeLeaseModel;

    /**
     * 变更后租金
     */
    @ApiModelProperty(value = "变更后租金")
    private BigDecimal chargeRentAmount;

    /**
     * 变更后押金
     */
    @ApiModelProperty(value = "变更后押金")
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
    @ApiModelProperty(value = "押金支付方式：1、押金账本支付;2、原车押金")
    private Integer depositPayType;

    @ApiModelProperty(value = "原车辆ID")
    private Integer sourceCarId;

    @ApiModelProperty(value = "原车牌号")
    private String sourcePlate;

    @ApiModelProperty(value = "未锁定押金账本金额")
    private BigDecimal unlockDepositAmount;

//    @ApiModelProperty(value = "押金支付方式TITLE")
//    public String getDepositPayTypeTitle() {
//        String title = "";
//        if (getDepositPayType() != null) {
//
//            if (ReplaceVehicleDepositPayTypeEnum.SOURCE_DEPOSIT_PAY.getCode() == getDepositPayType()) {
//                title = ReplaceVehicleDepositPayTypeEnum.SOURCE_DEPOSIT_PAY.getTitle();
//                title = String.format(title, plate);
//            } else {
//                title = ReplaceVehicleDepositPayTypeEnum.ACCOUNT_DEPOSIT_UNLOCK_PAY.getTitle();
//                title = String.format(title, chargeDepositAmount);
//            }
//        }
//        return title;
//    }
//
//    @ApiModelProperty(value = "押金支付方式枚举")
//    public Map<Integer, String> getDepositPayTypeMap() {
//
//        Map<Integer, String> map = Arrays.asList(ReplaceVehicleDepositPayTypeEnum.values())
//                .stream().collect(Collectors.toMap(ReplaceVehicleDepositPayTypeEnum::getCode, ReplaceVehicleDepositPayTypeEnum::getTitle));
//
//        map.forEach((key, value) -> {
//            if (ReplaceVehicleDepositPayTypeEnum.SOURCE_DEPOSIT_PAY.getCode() == key) {
//                value = String.format(value, plate);
//            } else {
//                value = String.format(value, String.valueOf(chargeDepositAmount != null ? chargeDepositAmount : 0));
//            }
//        });
//
//        return map;
//    }
}
