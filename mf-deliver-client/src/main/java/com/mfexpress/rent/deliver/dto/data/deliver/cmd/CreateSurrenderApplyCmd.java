package com.mfexpress.rent.deliver.dto.data.deliver.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@ApiModel(value = "向后市场端发起退保申请DTO")
@Data
public class CreateSurrenderApplyCmd {

    @ApiModelProperty(value = "申请来源")
    private Integer applySource = 2;

    @ApiModelProperty(value = "退保日期")
    private Date surrenderDate;

    @ApiModelProperty(value = "退保业务类型")
    private Integer applyType = 3;

    @ApiModelProperty(value = "操作人")
    private Integer applyUserId;

    @ApiModelProperty(value = "备注")
    private String acceptReason;

    @ApiModelProperty(value = "申请时间")
    private Date applyTime;

    @ApiModelProperty(value = "车辆及保险信息")
    private List<SurrenderInfoDTO> createH5SurrenderApplyCmdList;

    @ApiModel(value = "退保信息")
    @Data
    public static class SurrenderInfoDTO {

        @ApiModelProperty(value = "车牌号")
        private String plateNo;

        @ApiModelProperty(value = "车辆id")
        private Integer vehicleId;

        @ApiModelProperty(value = "申请原因")
        private String applyReason;

    }

}
