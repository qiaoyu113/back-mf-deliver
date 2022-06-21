package com.mfexpress.rent.deliver.serveadjust;

import com.mfexpress.rent.deliver.gateway.ServeAdjustGateway;
import com.mfexpress.rent.deliver.po.ServeAdjustPO;
import com.mfexpress.rent.deliver.serveadjust.repository.ServeAdjustMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class ServeAdjustGatewayImpl implements ServeAdjustGateway {

    @Resource
    ServeAdjustMapper serveAdjustMapper;

    @Override
    @Transactional
    public int save(ServeAdjustPO po) {

        return serveAdjustMapper.insertSelective(po);
    }

    @Override
    @Transactional
    public int updateByServeNo(ServeAdjustPO po) {

        return serveAdjustMapper.updateByExampleSelective(po, getServeNoExample(po.getServeNo()));
    }

    @Override
    public ServeAdjustPO getByServeNo(String serveNo) {

        return serveAdjustMapper.selectOneByExample(getServeNoExample(serveNo));
    }
}
