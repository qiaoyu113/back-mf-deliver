package com.mfexpress.rent.deliver.api;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import com.mfexpress.transportation.customer.dto.entity.vo.LinkmanVo;

public interface DeliverVehicleServiceI {

    Integer toDeliver(DeliverVehicleCmd deliverVehicleCmd, TokenInfo tokenInfo);

    LinkmanVo getDeliverByDeliverNo(Integer customerCmd);

    LinkmanVo getLinkmanByCustomerId(Integer customerId);

    Integer offlineDeliver(DeliverVehicleCmd cmd, TokenInfo tokenInfo);

}
