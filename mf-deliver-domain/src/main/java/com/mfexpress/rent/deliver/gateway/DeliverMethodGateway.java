package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.entity.DeliverMethodPO;

import java.util.List;

public interface DeliverMethodGateway {

    Integer saveDeliverMethods(List<DeliverMethodPO> deliverMethodPOS);

}
