package com.mfexpress.rent.deliver.dto.data.serve.qry;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yj
 * @date 2022/11/3 14:03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("将过期合同查询")
public class ContractWillExpireQry {

    @ApiModelProperty("状态")
    private List<Integer> statuses;

    @ApiModelProperty("预计收车日期")
    private List<String> expectRecoverDateList;

}
