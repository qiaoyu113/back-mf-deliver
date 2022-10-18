package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.entity.ServeRepairRecordPO;

import java.util.List;

public interface ServeRepairRecordGateway {

    Integer create(ServeRepairRecordPO serveRepairRecordPO);

    List<ServeRepairRecordPO> getListByServeNo(String serveNo);
}
