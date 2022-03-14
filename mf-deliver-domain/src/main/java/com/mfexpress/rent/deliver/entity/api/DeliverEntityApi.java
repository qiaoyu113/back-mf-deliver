package com.mfexpress.rent.deliver.entity.api;

import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCmd;

import java.util.List;

public interface DeliverEntityApi {

    List<DeliverDTO> getDeliverDTOListByServeNoList(List<String>serveNoList);

    void toHistory(ReactivateServeCmd cmd);
}
