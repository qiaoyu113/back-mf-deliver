package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;

@ApiModel("服务单押金信息")
@Data
public class ServeDepositDTO{

    private String serveNo;

    private String customerName;

    private String officeName;

    private String leaseModelDisplay;

    private String vehicleNum;

    private String brandDisplay;

    private String serveStatusDisplay;

    private String deliverVehicleDate;

    private String recoverVehicleDate;

    private BigDecimal payableDeposit;

    private BigDecimal paidInDeposit;

    private Boolean maintainConfirmFlag;

    private Boolean recoverConfirmFlag;
}
