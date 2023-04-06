package com.mfexpress.rent.deliver.delivervehicle;

import com.mfexpress.rent.deliver.delivervehicle.repository.DeliverMethodMapper;
import com.mfexpress.rent.deliver.entity.DeliverMethodPO;
import com.mfexpress.rent.deliver.gateway.DeliverMethodGateway;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class DeliverMethodGatewayImpl implements DeliverMethodGateway {

    @Resource
    private DeliverMethodMapper deliverMethodMapper;

    @Override
    public Integer saveDeliverMethods(List<DeliverMethodPO> deliverMethodPOS) {
        for (DeliverMethodPO deliverMethodPO : deliverMethodPOS) {
            deliverMethodMapper.insertSelective(deliverMethodPO);
        }
        return deliverMethodPOS.size();
    }

}
