package com.mfexpress.rent.deliver.dto.data;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yj
 * @date 2022/11/4 15:38
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("")
public class ContractExpireNotifyDTO {

    @ApiModelProperty("租赁合同循环模板信息")
    List<NoticeTemplateInfoDTO> loopTemplate;
}
