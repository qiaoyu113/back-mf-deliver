package com.mfexpress.rent.deliver.insuranceApply;

import com.google.common.collect.ImmutableMap;
import com.mfexpress.component.utils.util.MyBatisUtils;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.entity.InsuranceApplyPO;
import com.mfexpress.rent.deliver.gateway.InsuranceApplyGateway;
import com.mfexpress.rent.deliver.insuranceApply.repository.InsuranceApplyMapper;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@Component
public class InsuranceApplyGatewayImpl implements InsuranceApplyGateway {

    @Resource
    private InsuranceApplyMapper insuranceApplyMapper;

    @Override
    public void batchCreate(List<InsuranceApplyPO> insuranceApplyPOS) {
        for (InsuranceApplyPO insuranceApplyPO : insuranceApplyPOS) {
            InsuranceApplyPO originalApplyPO = getByDeliverNoAndType(insuranceApplyPO.getDeliverNo(), insuranceApplyPO.getType());
            if (null != originalApplyPO) {
                insuranceApplyPO.setId(originalApplyPO.getId());
                update(insuranceApplyPO);
            }else{
                insuranceApplyMapper.insertSelective(insuranceApplyPO);
            }
        }
    }

    @Override
    public InsuranceApplyPO getByDeliverNoAndType(String deliverNo, Integer type) {
        Example example = MyBatisUtils.createEqualExampleByMap(ImmutableMap.<String, Object>builder()
                .put("deliverNo", deliverNo)
                .put("type", type)
                .put("delFlag", JudgeEnum.NO.getCode())
                .build(), InsuranceApplyPO.class);
        return insuranceApplyMapper.selectOneByExample(example);
    }

    @Override
    public Integer update(InsuranceApplyPO applyPOToUpdate) {
        return insuranceApplyMapper.updateByPrimaryKeySelective(applyPOToUpdate);
    }

    @Override
    public Integer create(InsuranceApplyPO insuranceApplyPO) {
        return insuranceApplyMapper.insertSelective(insuranceApplyPO);
    }

    @Override
    public Integer del(Integer applyPOId) {
        InsuranceApplyPO applyPOToUpdate = new InsuranceApplyPO();
        applyPOToUpdate.setId(applyPOId);
        applyPOToUpdate.setDelFlag(JudgeEnum.YES.getCode());
        return update(applyPOToUpdate);
    }

    @Override
    public List<InsuranceApplyPO> getByDeliverNos(List<String> deliverNoList) {
        Example example = new Example(InsuranceApplyPO.class);
        example.createCriteria().andIn("deliverNo", deliverNoList)
                .andEqualTo("delFlag", JudgeEnum.NO.getCode());
        return insuranceApplyMapper.selectByExample(example);
    }

}
