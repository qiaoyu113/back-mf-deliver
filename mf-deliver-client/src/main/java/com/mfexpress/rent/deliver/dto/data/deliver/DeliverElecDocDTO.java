package com.mfexpress.rent.deliver.dto.data.deliver;

import lombok.Data;

@Data
public class DeliverElecDocDTO {

    private String deliverNo;

    private String deliverVehicleTime;

    private String recoverVehicleTime;

    private String deliverVehicleElecFileUrl;

    private String recoverVehicleElecFileUrl;

}
