package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@ApiModel(value = "电子交接合同操作记录和短信信息VO")
@Data
public class ElecContractOperationRecordWithSmsInfoVO {

    private List<ElecContractOperationRecordVO> records;

    private SmsInfoVO smsInfoVO;

    private Integer failureReason;

}
