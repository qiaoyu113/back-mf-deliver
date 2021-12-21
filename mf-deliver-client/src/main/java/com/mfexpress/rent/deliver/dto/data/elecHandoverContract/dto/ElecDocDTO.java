package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Column;

@ApiModel(value = "ElecContractDTO 电子交接单DTO")
@Data
public class ElecDocDTO {

    private Integer id;

    private Long contractId;

    private String deliverNo;

    private Integer deliverType;

    private String deliverOrRecoverNo;

    private String fileUrl;

    private Integer validStatus;

}
