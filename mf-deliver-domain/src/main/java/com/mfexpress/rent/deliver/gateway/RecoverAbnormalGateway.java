package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.dto.entity.RecoverAbnormal;

public interface RecoverAbnormalGateway {

    int create(RecoverAbnormal recoverAbnormal);

    RecoverAbnormal getRecoverAbnormalByServeNo(String serveNo);
}
