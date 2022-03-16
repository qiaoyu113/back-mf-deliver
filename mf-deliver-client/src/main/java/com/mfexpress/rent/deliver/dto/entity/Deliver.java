package com.mfexpress.rent.deliver.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @deprecated 不再用于实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
//@Table(name = "deliver")
@Builder
public class Deliver {
    private Integer id;

    private String deliverNo;

    private Long deliverId;

    private Integer customerId;

    private String serveNo;

    private Integer isCheck;

    private Integer isInsurance;

    private Integer isDeduction;

    private Integer insuranceRemark;

    private Integer carId;

    private Double mileage;

    private Double vehicleAge;

    private String carNum;

    private String frameNum;

    private Integer deliverStatus;

    private Integer status;

    private Integer saleId;

    private Integer carServiceId;

    private Integer createId;

    private Integer updateId;

    private Date createTime;

    private Date updateTime;

    private Integer deductionHandel;

    private Integer violationPoints;

    private BigDecimal deductionAmount;

    private BigDecimal agencyAmount;

    private String insuranceStartTime;

    private String insuranceEndTime;

    private Integer deliverContractStatus;

    private Integer recoverContractStatus;

    private Integer recoverAbnormalFlag;

}