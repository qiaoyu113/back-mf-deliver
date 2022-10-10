package com.mfexpress.rent.deliver.dto.data.deliver.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "申请applyIds查询")
public class ApplyByIdsQryCmd {

    @ApiModelProperty(value = "申请Ids")
    private List<String> applyIds;
}
