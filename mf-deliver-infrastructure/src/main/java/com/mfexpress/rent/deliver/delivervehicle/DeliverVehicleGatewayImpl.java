package com.mfexpress.rent.deliver.delivervehicle;


import com.mfexpress.rent.deliver.delivervehicle.repository.DeliverVehicleMapper;
import com.mfexpress.rent.deliver.entity.DeliverVehicleEntity;
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
    public int addDeliverVehicle(List<DeliverVehicleEntity> deliverVehicleList) {
        int i = 0;
        for (DeliverVehicleEntity deliverVehicle : deliverVehicleList) {
            i += deliverVehicleMapper.insertSelective(deliverVehicle);
        }
        return i;

    }

    @Override
    public DeliverVehicleEntity getDeliverVehicleByDeliverNo(String deliverNo) {

        Example example = new Example(DeliverVehicleEntity.class);
        example.createCriteria().andEqualTo("deliverNo", deliverNo);
        return deliverVehicleMapper.selectOneByExample(example);
    }

    @Override
    public DeliverVehicleEntity getDeliverVehicleOneByServeNo(String serveNoList) {
        Example example = new Example(DeliverVehicleEntity.class);
        example.createCriteria().andEqualTo("serveNo", serveNoList);

        return deliverVehicleMapper.selectOneByExample(example);
    }

    @Override
    public List<DeliverVehicleEntity> getDeliverVehicleByServeNo(List<String> serveNoList) {
        Example example = new Example(DeliverVehicleEntity.class);
        example.createCriteria().andIn("serveNo", serveNoList);

        return deliverVehicleMapper.selectByExample(example);
    }

    @Override
    public List<DeliverVehicleEntity> getDeliverVehicleByDeliverNoList(List<String> deliverNoList) {
        Example example = new Example(DeliverVehicleEntity.class);
        example.createCriteria().andIn("deliverNo", deliverNoList);
        return deliverVehicleMapper.selectByExample(example);

    }


}
