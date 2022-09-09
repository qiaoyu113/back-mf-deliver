package com.mfexpress.rent.deliver.dto.data.recovervehicle.vo;

import com.mfexpress.rent.deliver.dto.data.deliver.vo.TipVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "退保申请VO")
public class SurrenderApplyVO extends TipVO {

    @ApiModelProperty(value = "退保申请编号")
    private List<SurrenderApplyInfoVO> surrenderApplyInfoVOS;

}
