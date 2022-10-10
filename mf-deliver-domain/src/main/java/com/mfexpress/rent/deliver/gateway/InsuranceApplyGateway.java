package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.entity.InsuranceApplyPO;

import java.util.List;

public interface InsuranceApplyGateway {

    void batchCreate(List<InsuranceApplyPO> insuranceApplyPOS);

    InsuranceApplyPO getByDeliverNoAndType(String deliverNo, Integer type);

    Integer update(InsuranceApplyPO applyPOToUpdate);

    Integer create(InsuranceApplyPO insuranceApplyPO);

    List<InsuranceApplyPO> getByDeliverNos(List<String> deliverNoList);

    Integer del(Integer applyPOId);

}
