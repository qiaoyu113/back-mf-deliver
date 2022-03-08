package com.mfexpress.rent.deliver.entity;

import com.mfexpress.rent.deliver.entity.api.ServeEntityApi;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "serve")
@Builder
@Component
public class ServeEntity implements ServeEntityApi {
    @Id
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
}
