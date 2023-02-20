package com.mfexpress.rent.deliver.dto.data.deliver.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("车辆客户合同信息")
public class VehicleContractDTO {

    private String serveNo;

    private Integer customerId;

    private Long contractId;

    private Long vehicleId;

    private Integer contractCommodityId;

}
