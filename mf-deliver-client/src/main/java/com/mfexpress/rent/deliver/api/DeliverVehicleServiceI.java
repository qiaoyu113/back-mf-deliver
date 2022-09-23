package com.mfexpress.rent.deliver.api;

import java.util.List;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleVO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.cmd.DeliverVehicleProcessCmd;
import com.mfexpress.transportation.customer.dto.entity.vo.LinkmanVo;
import org.springframework.web.bind.annotation.RequestBody;

public interface DeliverVehicleServiceI {

    String toDeliver(DeliverVehicleCmd deliverVehicleCmd);

    LinkmanVo getDeliverByDeliverNo(Integer customerCmd);
    LinkmanVo getLinkmanByCustomerId(Integer customerId);
}
