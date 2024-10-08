package com.mfexpress.rent.deliver.dto.data.serve;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ServeDTO {
    private Integer id;

    private Long orderId;

    private String serveNo;

    private Long serveId;

    private Integer customerId;

    private Integer carModelId;

    private Integer leaseModelId;

    private Integer brandId;

    private Integer status;

    private Integer replaceFlag;

    private Integer carServiceId;

    private Integer saleId;

    private String remark;

    private Integer createId;

    private Integer updateId;

    private Integer cityId;

    private Integer orgId;

    private BigDecimal rent;

    private BigDecimal rentRatio;

    private BigDecimal deposit;

    private Date createTime;

    private Date updateTime;

    private String oaContractCode;

    private Integer goodsId;

    private Date leaseBeginDate;

    private Integer leaseMonths;

    private Integer leaseDays;

    private Date leaseEndDate;

    private String expectRecoverDate;

    private Integer renewalType;

    private String billingAdjustmentDate;

    private Integer reactiveFlag;

    private Integer contractCommodityId;

    private BigDecimal payableDeposit;

    private BigDecimal paidInDeposit;
}
