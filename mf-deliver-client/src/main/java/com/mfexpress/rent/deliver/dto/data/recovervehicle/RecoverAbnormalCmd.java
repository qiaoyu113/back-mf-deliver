package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import com.mfexpress.rent.deliver.dto.data.BaseCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@ApiModel("异常收车命令")
@Data
@EqualsAndHashCode(callSuper = true)
public class RecoverAbnormalCmd extends BaseCmd {

    @ApiModelProperty(value = "租赁服务单编号", required = true)
    @NotEmpty(message = "租赁服务单编号不能为空")
    private String serveNo;

    @ApiModelProperty(value = "电子合同id", required = true)
    @NotNull(message = "电子合同id不能为空")
    private Long elecContractId;

    @ApiModelProperty(value = "上报人id", required = true)
    @NotNull(message = "上报人不能为空")
    private Integer reporterId;

    @ApiModelProperty(value = "上报人姓名", required = true)
    @NotEmpty(message = "上报人不能为空")
    private String reporterName;

    @ApiModelProperty(value = "上报人手机号", required = true)
    @NotEmpty(message = "上报人手机号不能为空")
    private String reporterPhone;

    @ApiModelProperty(value = "原因", required = true)
    @NotEmpty(message = "原因不能为空")
    private String reason;

    @ApiModelProperty(value = "图片链接", required = true)
    @NotEmpty(message = "图片链接不能为空")
    private List<String> imgUrls;

    @ApiModelProperty(value = "收车时间", required = true)
    @NotNull(message = "收车时间不能为空")
    private Date recoverTime;

    private ElecContractDTO elecContractDTO;

}
