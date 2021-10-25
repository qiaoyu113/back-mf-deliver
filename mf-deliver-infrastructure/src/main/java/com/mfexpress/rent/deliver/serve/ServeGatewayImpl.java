package com.mfexpress.rent.deliver.serve;

import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.dto.data.serve.ServePreselectedDTO;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import com.mfexpress.rent.deliver.gateway.ServeGateway;
import com.mfexpress.rent.deliver.serve.repository.ServeMapper;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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
        serveMapper.insertList(serveList);
    }

    @Override
    public List<ServePreselectedDTO> getServePreselectedByOrderId(List<Long> orderId) {


        return serveMapper.getServePreselectedByOrderId(orderId);
    }

    @Override
    public List<String> getServeNoListAll() {

        return serveMapper.selectAll().stream().map(Serve::getServeNo).collect(Collectors.toList());
    }

    @Override
    public List<Serve> getServeByStatus() {
        Example example = new Example(Serve.class);
        example.createCriteria().orEqualTo("status", ServeEnum.DELIVER.getCode()).orEqualTo("status", ServeEnum.REPAIR.getCode());
        return serveMapper.selectByExample(example);


    }

    @Override
    public List<Serve> getCycleServe() {
        Example example = new Example(Serve.class);
        example.createCriteria().andGreaterThanOrEqualTo("status", ServeEnum.DELIVER.getCode());
        return serveMapper.selectByExample(example);


    }

    @Override
    public List<Serve> getServeByServeNoList(List<String> serveNoList) {
        Example example = new Example(Serve.class);
        example.createCriteria().andIn("serveNo", serveNoList);
        return serveMapper.selectByExample(example);
    }
}
