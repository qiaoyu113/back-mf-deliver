package com.mfexpress.rent.deliver.serve;

import com.mfexpress.rent.deliver.entity.ServeRepairRecordPO;
import com.mfexpress.rent.deliver.gateway.ServeRepairRecordGateway;
import com.mfexpress.rent.deliver.serve.repository.ServeRepairRecordMapper;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ServeRepairRecordGatewayImpl implements ServeRepairRecordGateway {

    @Resource
    private ServeRepairRecordMapper serveRepairRecordMapper;

    @Override
    public Integer create(ServeRepairRecordPO serveRepairRecordPO) {
        return serveRepairRecordMapper.insertSelective(serveRepairRecordPO);
    }

    @Override
    public List<ServeRepairRecordPO> getListByServeNo(String serveNo) {
        Example example = new Example(ServeRepairRecordPO.class);
        example.createCriteria().andEqualTo("serveNo", serveNo);
        example.orderBy("createTime").desc();
        return serveRepairRecordMapper.selectByExample(example);
    }
}
