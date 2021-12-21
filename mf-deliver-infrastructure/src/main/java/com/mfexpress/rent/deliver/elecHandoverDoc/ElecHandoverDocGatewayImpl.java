package com.mfexpress.rent.deliver.elecHandoverDoc;

import com.google.common.collect.ImmutableMap;
import com.mfexpress.component.utils.util.MyBatisUtils;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.po.ElectronicHandoverDocPO;
import com.mfexpress.rent.deliver.elecHandoverDoc.repository.ElecHandoverDocMapper;
import com.mfexpress.rent.deliver.gateway.ElecHandoverDocGateway;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ElecHandoverDocGatewayImpl implements ElecHandoverDocGateway {

    @Resource
    private ElecHandoverDocMapper docMapper;

    @Override
    public Map<String, String> batchCreate(List<ElectronicHandoverDocPO> docPOS) {
        Map<String, String> deliverNoWithDocId = new HashMap<>();
        docPOS.forEach(docPO -> {
            docMapper.insertSelective(docPO);
            deliverNoWithDocId.put(docPO.getDeliverNo(), docPO.getId().toString());
        });
        return deliverNoWithDocId;
    }

    @Override
    public int updateDocByDoc(ElectronicHandoverDocPO docPO) {
        Example example = MyBatisUtils.createEqualExampleByMap(ImmutableMap.<String, Object>builder()
                .put("contractId", docPO.getContractId())
                .build(), ElectronicHandoverDocPO.class);
        return docMapper.updateByExampleSelective(docPO, example);
    }

    @Override
    public int updateDocByDocId(Integer docId, ElectronicHandoverDocPO docPO) {
        Example example = MyBatisUtils.createEqualExampleByMap(ImmutableMap.<String, Object>builder()
                .put("id", docId)
                .build(), ElectronicHandoverDocPO.class);
        return docMapper.updateByExampleSelective(docPO, example);
    }

    @Override
    public ElectronicHandoverDocPO getDocByDeliverNoAndDeliverType(String deliverNo, Integer deliverType) {
        Example example = MyBatisUtils.createEqualExampleByMap(ImmutableMap.<String, Object>builder()
                .put("deliverNo", deliverNo)
                .put("deliverType", deliverType)
                .put("validStatus", JudgeEnum.YES.getCode())
                .build(), ElectronicHandoverDocPO.class);
        return docMapper.selectOneByExample(example);
    }

}
