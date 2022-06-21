package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.po.ServeAdjustRecordPO;

@Deprecated
public interface ServeAdjustRecordGateway {

    int saveRecord(ServeAdjustRecordPO record);

    ServeAdjustRecordPO getRecordByServeNo(String servNo);

    int updateRecord(ServeAdjustRecordPO po);
}
