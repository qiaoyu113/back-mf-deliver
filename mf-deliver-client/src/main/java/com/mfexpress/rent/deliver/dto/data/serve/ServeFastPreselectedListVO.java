package com.mfexpress.rent.deliver.dto.data.serve;


import com.mfexpress.rent.deliver.dto.data.ListVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("订单快速预选页")
public class ServeFastPreselectedListVO extends ListVO {

    @ApiModelProperty(value = "预选页数据")
    private List<ServeFastPreselectedVO> serveFastPreselectedVOList;


}
