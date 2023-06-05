package com.mfexpress.rent.deliver.dto.data.deliver.cmd;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author yj
 * @date 2023/3/7 11:03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("车辆违章 服务单信息查询")
public class VehicleViolationDeliverInfoQry {

    @NotNull
    @ApiModelProperty("车辆id")
    private Integer vehicleId;
    @NotNull
    @ApiModelProperty("违章时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date violationDate;

}
