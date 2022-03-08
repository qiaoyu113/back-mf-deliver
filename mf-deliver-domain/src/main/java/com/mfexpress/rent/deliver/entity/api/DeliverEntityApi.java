package com.mfexpress.rent.deliver.entity.api;

import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;

import java.util.List;

public interface DeliverEntityApi {

    List<DeliverDTO> getDeliverDTOListByServeNoList(List<String>serveNoList);
}
