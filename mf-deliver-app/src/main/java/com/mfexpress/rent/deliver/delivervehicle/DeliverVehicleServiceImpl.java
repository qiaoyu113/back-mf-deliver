package com.mfexpress.rent.deliver.delivervehicle;

import com.mfexpress.rent.deliver.api.DeliverVehicleServiceI;
import com.mfexpress.rent.deliver.delivervehicle.executor.DeliverVehicleExe;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DeliverVehicleServiceImpl implements DeliverVehicleServiceI {

    @Resource
    private DeliverVehicleExe deliverVehicleExe;

    @Resource
    private DeliverVehicleServiceI deliverVehicleServiceI;


    @Override
    public String toDeliver(DeliverVehicleCmd deliverVehicleCmd) {

        return deliverVehicleExe.execute(deliverVehicleCmd);
    }

    @Override
    public DeliverVehicleVO getDeliverByDeliverNo(String deliverNo) {
        return deliverVehicleExe.getDeliverByDeliverNo(deliverNo);
    }
}
