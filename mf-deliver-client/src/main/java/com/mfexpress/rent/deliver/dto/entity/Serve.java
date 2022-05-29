package com.mfexpress.rent.deliver.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**@deprecated
 * 不再作为实体使用
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Serve {

    private Integer id;

    private Long orderId;

    private Long serveId;

    private Integer customerId;

    private String serveNo;

    private Integer carModelId;

    private Integer leaseModelId;

    private Integer brandId;

    private Integer status;

    private Integer carServiceId;

    private Integer saleId;

    private String remark;

    private Integer createId;

    private Integer updateId;

    private Integer cityId;

    private Integer orgId;

    private BigDecimal rent;

    private Date createTime;

    private Date updateTime;

    private Integer replaceFlag;

    private Integer goodsId;

    // 续签合同迭代增加的字段-----------start
    private Long contractId;

    private String oaContractCode;

    private Double deposit;

    private String leaseBeginDate;

    private Integer leaseMonths;

    private String leaseEndDate;

    private String billingAdjustmentDate;

    private Integer renewalType;

    private String expectRecoverDate;
    // 续签合同迭代增加的字段-----------end

    private Integer contractCommodityId;

}