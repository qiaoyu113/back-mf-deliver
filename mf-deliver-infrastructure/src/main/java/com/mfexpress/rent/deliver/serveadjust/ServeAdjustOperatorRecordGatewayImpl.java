package com.mfexpress.rent.deliver.serveadjust;

import com.mfexpress.rent.deliver.gateway.ServeAdjustOperatorRecordGateway;
import com.mfexpress.rent.deliver.po.ServeAdjustOperatorRecordPO;
import com.mfexpress.rent.deliver.serveadjust.repository.ServeAdjustOperatorRecordMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ServeAdjustOperatorRecordGatewayImpl implements ServeAdjustOperatorRecordGateway {

    @Resource
    ServeAdjustOperatorRecordMapper serveAdjustOperatorRecordMapper;

    @Override
    public int save(ServeAdjustOperatorRecordPO po) {

        return serveAdjustOperatorRecordMapper.insertSelective(po);
    }
}
