package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo;

import com.mfexpress.rent.deliver.dto.data.serve.ServeVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@ApiModel(value = "电子交接合同列表展示VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ElecContractWithServesVO extends ElecDeliverContractVO {

    @ApiModelProperty(value = "租赁服务单列表")
    private List<ServeVO> serveVOList;

    private List<String> deliverNos;
}
