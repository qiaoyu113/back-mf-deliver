package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;

@ApiModel("服务单押金信息")
@Data
public class ServeDepositDTO {

    private String serveNo;


    private Integer customerId;

    private Integer orgId;

    private Integer leaseModelId;

    private String vehicleNum;

    private Integer brandId;

    private Integer status;

    private String deliverVehicleDate;

    private String recoverVehicleDate;

    private BigDecimal payableDeposit;

    private BigDecimal paidInDeposit;

    private Boolean maintainFeeConfirmFlag;
    private Boolean recoverFeeConfirmFlag;

}
