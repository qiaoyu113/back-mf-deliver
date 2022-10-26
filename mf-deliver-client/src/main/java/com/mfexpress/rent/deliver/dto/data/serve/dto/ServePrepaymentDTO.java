package com.mfexpress.rent.deliver.dto.data.serve.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hzq
 * @Package com.mfexpress.billing.pay.dto.data
 * @date 2022/10/19 09:19
 * @Copyright ©
 */
@Data
public class ServePrepaymentDTO {

    /**
     * 服务单编号
     */
    private String serveNo;
    /**
     * 预付款金额
     */
    private BigDecimal prepaymentAmount;
    /**
     * 客户Id
     */
    private Integer customerId;
    /**
     * 城市id
     */
    private Integer cityId;
    /**
     * 管理区id
     */
    private Integer orgId;

}
