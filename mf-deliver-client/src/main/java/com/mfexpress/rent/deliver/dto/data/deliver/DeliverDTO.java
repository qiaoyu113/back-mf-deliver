package com.mfexpress.rent.deliver.dto.data.deliver;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class DeliverDTO {
    private Integer id;

    private String deliverNo;

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

    private Date insuranceStartTime;

    private Date insuranceEndTime;

}
