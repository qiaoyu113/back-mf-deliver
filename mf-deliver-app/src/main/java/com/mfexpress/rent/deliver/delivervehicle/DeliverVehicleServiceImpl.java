package com.mfexpress.rent.deliver.delivervehicle;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.api.DeliverVehicleServiceI;
import com.mfexpress.rent.deliver.delivervehicle.executor.DeliverVehicleExe;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.entity.vo.LinkmanVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DeliverVehicleServiceImpl implements DeliverVehicleServiceI {

    @Resource
    private DeliverVehicleExe deliverVehicleExe;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;


    @Override
    public String toDeliver(DeliverVehicleCmd deliverVehicleCmd) {

        return deliverVehicleExe.execute(deliverVehicleCmd);
    }

    @Override
    public LinkmanVo getDeliverByDeliverNo(Integer customerId) {
        Result<List<LinkmanVo>> customerIds = customerAggregateRootApi.getLinkMansByCusomerId(customerId);
        List<LinkmanVo> objects = new ArrayList<>();
        List<LinkmanVo> data = customerIds.getData();
        if (data != null && data.size()>0) {
            for (LinkmanVo v : data) {
                if (v.getType() == 2) {
                    objects.add(v);
                }
            }
            return objects.get(0);
        }
        return new LinkmanVo();
    }
}
