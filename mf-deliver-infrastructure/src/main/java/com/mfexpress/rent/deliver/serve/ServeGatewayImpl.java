package com.mfexpress.rent.deliver.serve;

import com.mfexpress.rent.deliver.dto.entity.Serve;
import com.mfexpress.rent.deliver.gateway.ServeGateway;
import com.mfexpress.rent.deliver.serve.repository.ServeMapper;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ServeGatewayImpl implements ServeGateway {

    @Resource
    private ServeMapper serveMapper;


    @Override
    public int updateServeByServeNo(String serveNo, Serve serve) {
        Example example = new Example(Serve.class);
        example.createCriteria().andEqualTo("serveNo", serveNo);
        return serveMapper.updateByExampleSelective(serve, example);
    }

    @Override
    public int updateServeByServeNoList(List<String> serveNoList, Serve serve) {
        Example example = new Example(Serve.class);
        example.createCriteria().andIn("serveNo", serveNoList);
        return serveMapper.updateByExampleSelective(serve, example);

    }

    @Override
    public Serve getServeByServeNo(String serveNo) {
        Example example = new Example(Serve.class);
        example.createCriteria().andEqualTo("serveNo", serveNo);

        return serveMapper.selectOneByExample(example);
    }

    @Override
    public void addServeList(List<Serve> serveList) {
        for (Serve serve : serveList) {

            serveMapper.insertSelective(serve);
        }
    }
}
