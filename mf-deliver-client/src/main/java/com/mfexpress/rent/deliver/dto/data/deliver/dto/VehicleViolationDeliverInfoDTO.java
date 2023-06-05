package com.mfexpress.rent.deliver.dto.data.deliver.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author yj
 * @date 2023/3/7 11:03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("车辆违章 服务单信息")
public class VehicleViolationDeliverInfoDTO {

    private Integer vehicleId;
    private Long deliverId;
    private String deliverNo;

    private Long serveId;
    private String serveNo;
    private Long orderId;
    //    private String orderNo;
    private Long contractId;
    private String contractNo;
    private Integer customerId;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date deliverDate;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date recoverDate;

}
