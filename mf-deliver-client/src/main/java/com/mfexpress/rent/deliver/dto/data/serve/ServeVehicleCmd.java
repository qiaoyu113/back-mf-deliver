package com.mfexpress.rent.deliver.dto.data.serve;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("订单生成服务单具体车辆")
public class ServeVehicleCmd {

    private Integer carModelId;
    private Integer brandId;
    private Integer leaseModelId;
    private Integer num;
}
