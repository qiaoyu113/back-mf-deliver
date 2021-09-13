package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.dto.entity.Deliver;

import java.util.List;

public interface DeliverGateway {
    void addDeliver(List<Deliver> deliverList);

    void updateDeliverByServeNo(String serveNo, Deliver deliver);

    void updateDeliverByServeNoList(List<String> serveNoList, Deliver deliver);

    Deliver getDeliverByServeNo(String serveNo);


}
