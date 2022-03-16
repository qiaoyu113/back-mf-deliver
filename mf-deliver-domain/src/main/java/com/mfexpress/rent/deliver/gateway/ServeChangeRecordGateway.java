package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.entity.ServeChangeRecordPO;

import java.util.List;

public interface ServeChangeRecordGateway {

    void insertList(List<ServeChangeRecordPO> recordList);

    List<ServeChangeRecordPO> getList(String serveNo);
}
