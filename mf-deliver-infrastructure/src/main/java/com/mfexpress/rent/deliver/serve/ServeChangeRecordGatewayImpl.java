package com.mfexpress.rent.deliver.serve;

import com.mfexpress.rent.deliver.dto.entity.Serve;
import com.mfexpress.rent.deliver.dto.entity.ServeChangeRecord;
import com.mfexpress.rent.deliver.gateway.ServeChangeRecordGateway;
import com.mfexpress.rent.deliver.serve.repository.ServeChangeRecordMapper;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ServeChangeRecordGatewayImpl implements ServeChangeRecordGateway {

    @Resource
    private ServeChangeRecordMapper serveChangeRecordMapper;

    @Override
    public void insertList(List<ServeChangeRecord> recordList) {
        recordList.forEach(serveChangeRecord -> {
            serveChangeRecordMapper.insertSelective(serveChangeRecord);
        });
    }

    @Override
    public List<ServeChangeRecord> getList(String serveNo) {
        Example example = new Example(ServeChangeRecord.class);
        example.createCriteria().andEqualTo("serveNo", serveNo);

        return serveChangeRecordMapper.selectByExample(example);
    }
}
