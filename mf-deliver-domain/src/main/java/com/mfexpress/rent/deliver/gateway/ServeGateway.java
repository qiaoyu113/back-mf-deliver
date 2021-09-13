package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.dto.entity.Serve;

import java.util.List;

public interface ServeGateway {


    void updateServeByServeNo(String serveNo, Serve serve);

    void updateServeByServeNoList(List<String> serveNoList, Serve serve);

    Serve getServeByServeNo(String serveNo);
    void addServeList(List<Serve> serveList);
}
