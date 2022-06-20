package com.mfexpress.rent.deliver.serveadjustrecord;

import javax.annotation.Resource;

import com.google.common.collect.ImmutableMap;
import com.mfexpress.component.utils.util.MyBatisUtils;
import com.mfexpress.rent.deliver.gateway.ServeAdjustRecordGateway;
import com.mfexpress.rent.deliver.po.ServeAdjustRecordPO;
import com.mfexpress.rent.deliver.serveadjustrecord.repository.ServeAdjustRecordMapper;
import org.springframework.stereotype.Component;

@Deprecated
@Component
public class ServeAdjustRecordGatewayImpl implements ServeAdjustRecordGateway {

    @Resource
    private ServeAdjustRecordMapper serveAdjustRecordMapper;

    @Override
    public int saveRecord(ServeAdjustRecordPO record) {

        return serveAdjustRecordMapper.insert(record);
    }

    @Override
    public ServeAdjustRecordPO getRecordByServeNo(String servNo) {

        return serveAdjustRecordMapper.selectOneByExample(MyBatisUtils
                .createEqualExampleByMap(ImmutableMap.<String, Object>builder()
                        .put("serveNo", servNo).build(), ServeAdjustRecordPO.class));
    }

    @Override
    public int updateRecord(ServeAdjustRecordPO po) {

        return serveAdjustRecordMapper.updateByPrimaryKeySelective(po);
    }
}
