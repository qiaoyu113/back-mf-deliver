package com.mfexpress.rent.deliver.domainservice;

import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositListDTO;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositLockListDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDepositDTO;

import java.util.List;

public interface ServeDomainServiceI {
  PagePagination<ServeDepositDTO> getPageServeDeposit(CustomerDepositListDTO customerDepositLisDTO);

  List<CustomerDepositLockListDTO> getCustomerDepositLockList(List<String> serveNoList);
}
