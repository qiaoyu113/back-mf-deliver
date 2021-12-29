package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.dto.entity.Deliver;

import java.util.List;

public interface DeliverGateway {
    int addDeliver(List<Deliver> deliverList);

    int updateDeliverByServeNo(String serveNo, Deliver deliver);

    int updateDeliverByServeNoList(List<String> serveNoList, Deliver deliver);

    Deliver getDeliverByServeNo(String serveNo);

    Deliver getDeliverByCarIdAndDeliverStatus(Integer carId, List<Integer> deliverStatus);

    int updateInsuranceStatusByCarId(List<Integer> carId, Integer status1, Integer status2);

    int updateMileageAndVehicleAgeByCarId(Integer carId, Deliver deliver);

    List<Deliver> getDeliverDeductionByServeNoList(List<String> serveNoList);

    List<Deliver> getDeliverByServeNoList(List<String> serveNoList);

    List<Deliver>getDeliverByDeductStatus(List<String>serveNoList);

    int updateDeliverByDeliverNos(List<String> deliverNos, Deliver deliver);

    Deliver getDeliverByDeliverNo(String deliverNo);

    List<Deliver> getDeliverByDeliverNoList(List<String> deliverNos);

    List<Deliver> getDeliverByCarId(Integer carId);

}
