package com.mfexpress.rent.deliver.dto.data;

import cn.hutool.core.collection.CollUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yj
 * @date 2022/11/3 15:07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("通知模板信息dto")
public class NoticeTemplateInfoDTO {


    @ApiModelProperty("客户名称")
    private String customerName;
    @ApiModelProperty("车牌号列表")
    private List<String> licensePlateList;

    @ApiModelProperty(value = "车数量", hidden = true)
    private Integer carNumber;
//
//    private String contractNo;
//
//    private String expectRecoverDate;


    public Integer getCarNumber() {
        if (CollUtil.isEmpty(licensePlateList)) {
            return 0;
        }
        return licensePlateList.size();
    }
}
