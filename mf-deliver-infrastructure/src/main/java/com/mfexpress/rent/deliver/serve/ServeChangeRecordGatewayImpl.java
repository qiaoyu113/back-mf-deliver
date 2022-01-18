package com.mfexpress.rent.deliver.serve;

import com.mfexpress.rent.deliver.dto.entity.ServeChangeRecord;
import com.mfexpress.rent.deliver.gateway.ServeChangeRecordGateway;
import com.mfexpress.rent.deliver.serve.repository.ServeChangeRecordMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ServeChangeRecordGatewayImpl implements ServeChangeRecordGateway {

    @Resource
    private ServeChangeRecordMapper serveChangeRecordMapper;

    @Override
    public void insertList(List<ServeChangeRecord> recordList) {
        serveChangeRecordMapper.insertList(recordList);
    }
}
