package com.mfexpress.rent.deliver.delivervehicle;


import com.mfexpress.rent.deliver.delivervehicle.repository.DeliverVehicleMapper;
import com.mfexpress.rent.deliver.dto.entity.DeliverVehicle;
import com.mfexpress.rent.deliver.gateway.DeliverVehicleGateway;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@Component
public class DeliverVehicleGatewayImpl implements DeliverVehicleGateway {

    @Resource
    private DeliverVehicleMapper deliverVehicleMapper;

    @Override
    public int addDeliverVehicle(List<DeliverVehicle> deliverVehicleList) {
        int i = 0;
        for (DeliverVehicle deliverVehicle : deliverVehicleList) {
            i += deliverVehicleMapper.insertSelective(deliverVehicle);
        }
        return i;

    }

    @Override
    public DeliverVehicle getDeliverVehicleByDeliverNo(String deliverNo) {

        Example example = new Example(DeliverVehicle.class);
        example.createCriteria().andEqualTo("deliverNo", deliverNo);
        return deliverVehicleMapper.selectOneByExample(example);
    }
}
