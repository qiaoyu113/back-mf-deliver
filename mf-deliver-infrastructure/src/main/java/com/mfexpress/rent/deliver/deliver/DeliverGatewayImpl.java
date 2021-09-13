package com.mfexpress.rent.deliver.deliver;

import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.deliver.repository.DeliverMapper;
import com.mfexpress.rent.deliver.dto.entity.Deliver;
import com.mfexpress.rent.deliver.gateway.DeliverGateway;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@Component
public class DeliverGatewayImpl implements DeliverGateway {

    @Resource
    private DeliverMapper deliverMapper;

    @Override
    public void addDeliver(List<Deliver> deliverList) {
        for (Deliver deliver : deliverList) {
            deliverMapper.insertSelective(deliver);
        }
    }

    @Override
    public void updateDeliverByServeNo(String serveNo, Deliver deliver) {
        Example example = new Example(Deliver.class);
        example.createCriteria().andEqualTo("serveNo", serveNo).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        deliverMapper.updateByExampleSelective(deliver, example);
    }

    @Override
    public void updateDeliverByServeNoList(List<String> serveNoList, Deliver deliver) {
        Example example = new Example(Deliver.class);
        example.createCriteria().andIn("serveNo", serveNoList).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        deliverMapper.updateByExampleSelective(deliver, example);
    }


    @Override
    public Deliver getDeliverByServeNo(String serveNo) {
        Example example = new Example(Deliver.class);
        example.createCriteria().andEqualTo("serveNo", serveNo).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        return deliverMapper.selectOneByExample(example);

    }


}
