package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositListDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeListQry;
import com.mfexpress.rent.deliver.dto.data.serve.ServePreselectedDTO;
import com.mfexpress.rent.deliver.entity.ServeEntity;

import java.util.List;
import java.util.Map;

public interface ServeGateway {


    int updateServeByServeNo(String serveNo, ServeEntity serve);

    int updateServeByServeNoList(List<String> serveNoList, ServeEntity serve);

    ServeEntity getServeByServeNo(String serveNo);

    void addServeList(List<ServeEntity> serveList);

    List<ServePreselectedDTO> getServePreselectedByOrderId(List<Long> orderId);
    List<String> getServeNoListAll();

    List<ServeEntity>getServeByStatus();
    List<ServeEntity>getCycleServe(List<Integer> customerIdList);
    List<ServeEntity> getServeByServeNoList(List<String> serveNoList);

    List<ServeEntity> getServeListByOrderIds(List<Long> orderIds);

    Integer getCountByQry(ServeListQry qry);

    PagePagination<ServeEntity> getPageServeByQry(ServeListQry qry);

    void batchUpdate(List<ServeEntity> serveToUpdateList);
    List<ServeEntity>getServeByCustomerIdDeliver(List<Integer>customerIdList);
    List<ServeEntity>getServeByCustomerIdRecover(List<Integer>customerIdList);

    ServeEntity getServeDepositByServeNo(CustomerDepositListDTO qry);

    PagePagination<ServeEntity>pageServeDeposit(CustomerDepositListDTO qry);

    Map<Integer,Integer> getReplaceNumByCustomerIds(List<Integer> customerIds);
}
