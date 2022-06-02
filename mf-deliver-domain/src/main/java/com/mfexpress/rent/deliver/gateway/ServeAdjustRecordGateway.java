package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeAdjustRecordVo;
import com.mfexpress.rent.deliver.po.ServeAdjustRecordPO;

public interface ServeAdjustRecordGateway {

    int saveRecord(ServeAdjustRecordPO record);

    ServeAdjustRecordPO getRecordByServeNo(String servNo);

    int updateRecord(ServeAdjustRecordPO po);
}
