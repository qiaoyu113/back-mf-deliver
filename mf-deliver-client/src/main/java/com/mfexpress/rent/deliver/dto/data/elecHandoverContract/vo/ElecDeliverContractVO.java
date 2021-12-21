package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo;

import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel(value = "发车电子交接合同VO")
@Data
public class ElecDeliverContractVO {

    @ApiModelProperty(value = "电子交接合同全局id")
    private Long elecContractId;

    @ApiModelProperty(value = "电子交接合同对应的契约锁合同编号")
    private String elecContractNo;

    @ApiModelProperty(value = "状态，0:生成中，1:已生成/签署中，2:已完成，3:失败")
    private Integer elecContractStatus;

    @ApiModelProperty(value = "电子合同失败原因，1：作废，2：过期，3其他")
    private Integer elecContractFailureReason;

    // 发车单信息
    @ApiModelProperty(value = "发车单信息")
    private DeliverInfo deliverInfo;

    // 收车单信息
    /*@ApiModelProperty(value = "收车单信息")
    private RecoverInfo recoverInfo;*/

    // 人车合照列表
    @ApiModelProperty(value = "人车合照列表")
    private List<GroupPhotoVO> groupPhotoVOS;

    @ApiModelProperty(value = "是否可重发短信，1：是，0：否")
    private Integer sendSmsFlag;

    @ApiModelProperty(value = "可重发短信的情况下，短信还剩多少秒可发送")
    private Integer smsCountDown;

}
