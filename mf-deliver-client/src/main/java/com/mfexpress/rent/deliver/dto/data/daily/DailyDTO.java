package com.mfexpress.rent.deliver.dto.data.daily;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel(value = "日报数据")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyDTO {

    private String serveNo;
    private Integer vehicleId;

    private Integer leaseModelId;
    private Integer customerId;

    private Integer orgId;
    private String rentDate;

    private Integer chargeFlag;

    private Integer delFlag;

    private String carNum;
    private Integer replaceFlag;
    private Integer repairFlag;
}
