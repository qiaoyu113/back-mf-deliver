package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.dto.entity.Deliver;

import java.util.List;

public interface DeliverGateway {
    int addDeliver(List<Deliver> deliverList);

    int updateDeliverByServeNo(String serveNo, Deliver deliver);

    int updateDeliverByServeNoList(List<String> serveNoList, Deliver deliver);

    Deliver getDeliverByServeNo(String serveNo);

    Deliver getDeliverByCarId(Integer carId);

    int updateInsuranceStatusByCarId(Integer carId,Integer status1,Integer status2);

    int updateMileageByCarId(Integer carId,Double mileage);


}
