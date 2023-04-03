package com.mfexpress.rent.deliver.dto.data.deliver;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.DeliverBatchInsureApplyDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeliverInsureCmd extends BaseCmd {

    @ApiModelProperty(value = "租赁服务单编号列表", required = true)
    @NotEmpty(message = "服务单编号不能为空")
    private List<String> serveNoList;

    @ApiModelProperty(value = "车辆id列表", required = true)
    @NotEmpty(message = "车辆id不能为空")
    private List<Integer> carIdList;

    @ApiModelProperty(value = "开始投保日期", required = true)
    @JsonFormat(timezone = "GMT+8")
    @NotNull(message = "投保起始时间不能为空")
    private Date startInsureDate;

    @ApiModelProperty(value = "结束投保日期", required = true)
    @JsonFormat(timezone = "GMT+8")
    @NotNull(message = "投保结束时间不能为空")
    private Date endInsureDate;

    private Integer carServiceId;

    @ApiModelProperty(value = "发车批量投保申请DTO")
    private DeliverBatchInsureApplyDTO deliverBatchInsureApplyDTO;

    @ApiModelProperty(value = "保费承担方", required = true)
    @NotNull(message = "保费承担方不能为空")
    private Integer premiumUndertaker;

    @ApiModelProperty("投保公司")
    private Integer insureCompanyId;

    @ApiModelProperty("被保公司")
    private Integer insuredCompanyId;

}

