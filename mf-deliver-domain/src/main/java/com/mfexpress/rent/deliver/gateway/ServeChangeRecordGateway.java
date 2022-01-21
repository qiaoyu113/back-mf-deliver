package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.dto.entity.ServeChangeRecord;

import java.util.List;

public interface ServeChangeRecordGateway {

    void insertList(List<ServeChangeRecord> recordList);
}
