package com.mfexpress.rent.deliver.deliver;

import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
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
    public int addDeliver(List<Deliver> deliverList) {
        int i = 0;
        for (Deliver deliver : deliverList) {
            i += deliverMapper.insertSelective(deliver);
        }
        return i;
    }

    @Override
    public int updateDeliverByServeNo(String serveNo, Deliver deliver) {
        Example example = new Example(Deliver.class);
        example.createCriteria().andEqualTo("serveNo", serveNo).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        int i = deliverMapper.updateByExampleSelective(deliver, example);
        return i;
    }

    @Override
    public int updateDeliverByServeNoList(List<String> serveNoList, Deliver deliver) {
        Example example = new Example(Deliver.class);
        example.createCriteria().andIn("serveNo", serveNoList).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        int i = deliverMapper.updateByExampleSelective(deliver, example);
        return i;
    }


    @Override
    public Deliver getDeliverByServeNo(String serveNo) {
        Example example = new Example(Deliver.class);
        example.createCriteria().andEqualTo("serveNo", serveNo).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        return deliverMapper.selectOneByExample(example);

    }

    @Override
    public Deliver getDeliverByCarId(Integer carId) {
        Example example = new Example(Deliver.class);
        example.createCriteria().andEqualTo("carId", carId).andEqualTo("status", ValidStatusEnum.VALID.getCode())
                .andEqualTo("deliverStatus", DeliverEnum.IS_DELIVER.getCode());
        return deliverMapper.selectOneByExample(example);
    }

    @Override
    public int updateInsuranceStatusByCarId(Integer carId, Integer status1, Integer status2) {
        Example example = new Example(Deliver.class);
        //发车中交付单
        example.createCriteria().andEqualTo("carId", carId).andEqualTo("deliverStatus", DeliverEnum.IS_DELIVER.getCode());
        Deliver deliver = Deliver.builder().isInsurance(status1).build();
        int i = deliverMapper.updateByExampleSelective(deliver, example);
        Example example1 = new Example(Deliver.class);
        //收车中交付单
        example1.createCriteria().andEqualTo("carId", carId).andNotEqualTo("deliverStatus", DeliverEnum.IS_DELIVER.getCode());
        Deliver deliver1 = Deliver.builder().isInsurance(status2).build();
        int j = deliverMapper.updateByExampleSelective(deliver1, example1);
        return i + j;
    }

    @Override
    public int updateMileageByCarId(Integer carId, Double mileage) {

        Example example = new Example(Deliver.class);
        example.createCriteria().andEqualTo("carId", carId);
        Deliver deliver = Deliver.builder().mileage(mileage).build();
        return deliverMapper.updateByExampleSelective(deliver, example);
    }

    @Override
    public List<Deliver> getDeliverDeductionByServeNoList(List<String> serveNoList) {
        //查询已经处理违章的交付单
        Example example = new Example(Deliver.class);
        example.createCriteria().andIn("serveNo", serveNoList).andEqualTo("isDeduction", JudgeEnum.YES.getCode());
        return deliverMapper.selectByExample(example);
    }


}
