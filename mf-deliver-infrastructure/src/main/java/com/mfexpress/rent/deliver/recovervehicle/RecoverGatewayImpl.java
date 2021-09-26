package com.mfexpress.rent.deliver.recovervehicle;

import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.dto.entity.RecoverVehicle;
import com.mfexpress.rent.deliver.gateway.RecoverVehicleGateway;
import com.mfexpress.rent.deliver.recovervehicle.repository.RecoverVehicleMapper;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RecoverGatewayImpl implements RecoverVehicleGateway {

    @Resource
    private RecoverVehicleMapper recoverVehicleMapper;

    @Override
    public void addRecoverVehicle(List<RecoverVehicle> recoverVehicleList) {
        for (RecoverVehicle recoverVehicle : recoverVehicleList) {
            recoverVehicleMapper.insertSelective(recoverVehicle);
        }

    }

    @Override
    public int updateRecoverVehicle(RecoverVehicle recoverVehicle) {
        Example example = new Example(RecoverVehicle.class);
        example.createCriteria().andEqualTo("serveNo", recoverVehicle.getServeNo())
                .andEqualTo("status", ValidStatusEnum.VALID.getCode());
        int i = recoverVehicleMapper.updateByExampleSelective(recoverVehicle, example);
        return i;
    }

    @Override
    public List<RecoverVehicle> selectRecoverByServeNoList(List<String> serveNoList) {
        Example example = new Example(RecoverVehicle.class);
        example.createCriteria().andIn("serveNo", serveNoList)
                .andEqualTo("status", ValidStatusEnum.VALID.getCode());
        return recoverVehicleMapper.selectByExample(example);
    }

    @Override
    public RecoverVehicle getRecoverVehicleByDeliverNo(String deliverNo) {

        Example example = new Example(RecoverVehicle.class);
        example.createCriteria().andEqualTo("deliverNo", deliverNo).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        return recoverVehicleMapper.selectOneByExample(example);
    }


}
