package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.entity.DeliverEntity;

import java.util.List;

public interface DeliverGateway {
    int addDeliver(List<DeliverEntity> deliverList);

    int updateDeliverByServeNo(String serveNo, DeliverEntity deliver);

    int updateDeliverByServeNoList(List<String> serveNoList, DeliverEntity deliver);

    DeliverEntity getDeliverByServeNo(String serveNo);

    DeliverEntity getDeliverByCarIdAndDeliverStatus(Integer carId, List<Integer> deliverStatus);

    int updateInsuranceStatusByCarId(List<Integer> carId, Integer status1, Integer status2);

    int updateMileageAndVehicleAgeByCarId(Integer carId, DeliverEntity deliver);

    List<DeliverEntity> getDeliverDeductionByServeNoList(List<String> serveNoList);

    List<DeliverEntity> getDeliverByServeNoList(List<String> serveNoList);

    List<DeliverEntity>getDeliverByDeductStatus(List<String>serveNoList);

    int updateDeliverByDeliverNos(List<String> deliverNos, DeliverEntity deliver);

    DeliverEntity getDeliverByDeliverNo(String deliverNo);

    List<DeliverEntity> getDeliverByDeliverNoList(List<String> deliverNos);

    List<DeliverEntity> getDeliverByCarId(Integer carId);

    List<DeliverEntity> getDeliverDTOSByCarIdList(List<Integer> carIds);

    List<DeliverEntity>getDeliverNotCompleteByServeNoList(List<String>serveNoList);

    List<DeliverEntity> getMakeDeliverDTOSByCarIdList(List<Integer> carIds, Integer type);



}
