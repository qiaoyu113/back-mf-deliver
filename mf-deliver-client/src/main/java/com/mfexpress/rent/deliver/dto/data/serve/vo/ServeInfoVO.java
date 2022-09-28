package com.mfexpress.rent.deliver.dto.data.serve.vo;

import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverEachLeaseTermAmountVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author hzq
 * @Package com.mfexpress.rent.deliver.dto.data.serve.vo
 * @date 2022/9/28 10:02
 * @Copyright ©
 */
@Data
@ApiModel(value = "服务单详情")
public class ServeInfoVO {

    @ApiModelProperty(value = "数据")
    private PagePagination<DeliverEachLeaseTermAmountVO> data;

    @ApiModelProperty(value = "操作记录")
    private List<ServeOperationRecordVO> recordVOS;
}
