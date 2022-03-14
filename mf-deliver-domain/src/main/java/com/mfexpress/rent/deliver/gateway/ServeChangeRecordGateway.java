package com.mfexpress.rent.deliver.gateway;


import com.mfexpress.rent.deliver.entity.ServeChangeRecordPO;

import java.util.List;

public interface ServeChangeRecordGateway {

    void insertList(List<ServeChangeRecordPO> recordList);

    List<ServeChangeRecordPO> getList(String serveNo, Integer type);

    void insert(ServeChangeRecordPO serveChangeRecordPO);

    List<ServeChangeRecordPO> getListByServeNoListAndType(List<String> serveNoList, Integer type);

}
