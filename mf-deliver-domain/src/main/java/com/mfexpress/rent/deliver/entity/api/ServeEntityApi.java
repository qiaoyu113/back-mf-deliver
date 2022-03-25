package com.mfexpress.rent.deliver.entity.api;

import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositListDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDepositDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ServeEntityApi {

    /**
     * 存在指定车牌号 根据服务单编号以及查询条件查询
     *
     * @param customerDepositLisDTO 查询条件
     * @return
     */
    ServeDepositDTO getServeDepositByServeNo(CustomerDepositListDTO customerDepositLisDTO);

    /**
     * 根据条件查询 客户服务单押金列表 不包括车牌号
     *
     * @param customerDepositLisDTO 查询条件
     * @return
     */
    PagePagination<ServeDepositDTO> getServeDepositByQry(CustomerDepositListDTO customerDepositLisDTO);

    List<ServeDTO>getServeListByServeNoList(List<String>serveNoList);

    ServeDTO getServeByServeNo(String serveNo);

    void updateServeDepositByServeNoList(Map<String, BigDecimal> updateDepositMap, Integer creatorId,Boolean isLockFlag);
}
