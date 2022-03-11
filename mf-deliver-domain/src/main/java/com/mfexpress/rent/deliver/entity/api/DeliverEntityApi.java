package com.mfexpress.rent.deliver.entity.api;

import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;

import java.util.List;

public interface DeliverEntityApi {

    List<DeliverDTO> getDeliverDTOListByServeNoList(List<String> serveNoList);

    DeliverDTO getDeliverDTOByCarId(Integer carId);

    /**
     * 根据服务单编号查询未完成（存在费用未处理）得交付单
     *
     * @param serveNoList 服务单编号
     * @return 交付单
     */
    List<DeliverDTO> getDeliverNotComplete(List<String> serveNoList);

    DeliverDTO getDeliverByDeliverNo(String deliverNo);
}
