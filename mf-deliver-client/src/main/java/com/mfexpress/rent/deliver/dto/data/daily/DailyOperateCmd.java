package com.mfexpress.rent.deliver.dto.data.daily;

import com.mfexpress.rent.deliver.dto.entity.Deliver;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("收发车日报处理")
@Builder
public class DailyOperateCmd {

    @ApiModelProperty("发车服务单信息")
    private List<Serve> serveList;
    @ApiModelProperty("交付单信息")
    private Map<String, Deliver> deliverMap;
    @ApiModelProperty("收发车日期")
    private Date date;

}
