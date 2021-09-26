package com.mfexpress.rent.deliver.delivervehicle;

import com.mfexpress.rent.deliver.api.DeliverVehicleServiceI;
import com.mfexpress.rent.deliver.delivervehicle.executor.DeliverVehicleExe;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DeliverVehicleServiceImpl implements DeliverVehicleServiceI {

    @Resource
    private DeliverVehicleExe deliverVehicleExe;

    @Override
    public String toDeliver(DeliverVehicleCmd deliverVehicleCmd) {

        return deliverVehicleExe.execute(deliverVehicleCmd);
    }
}
