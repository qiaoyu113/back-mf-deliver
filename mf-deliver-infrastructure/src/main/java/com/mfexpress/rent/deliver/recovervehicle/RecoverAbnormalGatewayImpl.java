package com.mfexpress.rent.deliver.recovervehicle;

import com.mfexpress.rent.deliver.dto.entity.RecoverAbnormal;
import com.mfexpress.rent.deliver.dto.entity.RecoverVehicle;
import com.mfexpress.rent.deliver.gateway.RecoverAbnormalGateway;
import com.mfexpress.rent.deliver.recovervehicle.repository.RecoverAbnormalMapper;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;

@Component
public class RecoverAbnormalGatewayImpl implements RecoverAbnormalGateway {

    @Resource
    private RecoverAbnormalMapper recoverAbnormalMapper;

    @Override
    public int create(RecoverAbnormal recoverAbnormal) {
        recoverAbnormalMapper.insertSelective(recoverAbnormal);
        return recoverAbnormal.getId();
    }

    @Override
    public RecoverAbnormal getRecoverAbnormalByServeNo(String serveNo) {
        Example example = new Example(RecoverVehicle.class);
        example.createCriteria().andEqualTo("serveNo", serveNo);
        return recoverAbnormalMapper.selectOneByExample(example);
    }
}
