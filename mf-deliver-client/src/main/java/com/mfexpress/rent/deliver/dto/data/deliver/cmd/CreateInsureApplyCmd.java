package com.mfexpress.rent.deliver.dto.data.deliver.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@ApiModel(value = "向后市场端发起投保申请DTO")
@Data
public class CreateInsureApplyCmd {

    @ApiModelProperty(value = "申请来源")
    private Integer applySource = 2;

    @ApiModelProperty(value = "投保开始日期")
    private Date startInsureDate;

    @ApiModelProperty(value = "投保结束日期")
    private Date endInsureDate;

    @ApiModelProperty(value = "投保业务类型")
    private Integer insureBusinessType = 2;

    @ApiModelProperty(value = "操作人")
    private Integer operatorUserId;

    @ApiModelProperty(value = "车辆及保险信息")
    private List<InsureInfoDTO> insuranceApplyList;

    @ApiModelProperty(value = "备注")
    private String remarks;

    @ApiModelProperty(value = "操作时间")
    private Date operatorTime;

    @ApiModel(value = "投保信息")
    @Data
    public static class InsureInfoDTO {

        @ApiModelProperty(value = "车辆id")
        private Integer vehicleId;

        @ApiModelProperty(value = "车牌号")
        private String plate;

        @ApiModelProperty(value = "申请原因")
        private String applyReason;

        @ApiModelProperty(value = "座位险保额")
        private Integer seatInsurance;

        @ApiModelProperty(value = "三者险保额")
        private Integer thirdInsurance;

        @ApiModelProperty(value = "车损险标识")
        private Integer damageFlag;

        @ApiModelProperty(value = "保费承担方")
        private Integer premiumUndertaker;

        @ApiModelProperty(value = "车辆orgId")
        private Integer orgId;

        /**
         * 乘客座位数
         */
        @ApiModelProperty(value = "乘客座位数 prd1.13新增")
        private Integer passengerSeatNum;

        /**
         * 乘客座位险
         */
        @ApiModelProperty(value = "乘客座位险 prd1.13新增")
        private Integer passengerSeatInsurance;
    }

}
