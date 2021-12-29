package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.dto.data.serve.ServePreselectedDTO;
import com.mfexpress.rent.deliver.dto.entity.Serve;

import java.util.List;

public interface ServeGateway {


    int updateServeByServeNo(String serveNo, Serve serve);

    int updateServeByServeNoList(List<String> serveNoList, Serve serve);

    Serve getServeByServeNo(String serveNo);

    void addServeList(List<Serve> serveList);

    List<ServePreselectedDTO> getServePreselectedByOrderId(List<Long> orderId);
    List<String> getServeNoListAll();

    List<Serve>getServeByStatus();
    List<Serve>getCycleServe(List<Integer> customerIdList);
    List<Serve> getServeByServeNoList(List<String> serveNoList);

    List<Serve> getServeListByOrderIds(List<Long> orderIds);




}
