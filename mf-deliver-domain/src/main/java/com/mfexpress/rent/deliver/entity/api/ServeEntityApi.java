package com.mfexpress.rent.deliver.entity.api;

import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCmd;

public interface ServeEntityApi {

    void reactiveServe(ReactivateServeCmd cmd);
}
