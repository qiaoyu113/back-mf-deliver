package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.po.ElectronicHandoverDocPO;

import java.util.List;
import java.util.Map;

public interface ElecHandoverDocGateway {

    Map<String, String> batchCreate(List<ElectronicHandoverDocPO> docPOS);

    int updateDocByDoc(ElectronicHandoverDocPO docPO);

    int updateDocByDocId(Integer docId, ElectronicHandoverDocPO docPO);
}
