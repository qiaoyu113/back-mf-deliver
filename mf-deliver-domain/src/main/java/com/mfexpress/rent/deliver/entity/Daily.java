package com.mfexpress.rent.deliver.entity;

import lombok.Data;

import javax.persistence.Table;

@Table(name = "daily")
@Data
public class Daily {
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
