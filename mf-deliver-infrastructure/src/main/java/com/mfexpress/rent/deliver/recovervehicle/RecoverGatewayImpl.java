package com.mfexpress.rent.deliver.recovervehicle;

import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.entity.RecoverVehicleEntity;
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
    public void addRecoverVehicle(List<RecoverVehicleEntity> recoverVehicleList) {
        for (RecoverVehicleEntity recoverVehicle : recoverVehicleList) {
            recoverVehicleMapper.insertSelective(recoverVehicle);
        }

    }

    @Override
    public int updateRecoverVehicle(RecoverVehicleEntity recoverVehicle) {
        Example example = new Example(RecoverVehicleEntity.class);
        example.createCriteria().andEqualTo("serveNo", recoverVehicle.getServeNo())
                .andEqualTo("status", ValidStatusEnum.VALID.getCode());
        int i = recoverVehicleMapper.updateByExampleSelective(recoverVehicle, example);
        return i;
    }

    @Override
    public List<RecoverVehicleEntity> selectRecoverByServeNoList(List<String> serveNoList) {
        Example example = new Example(RecoverVehicleEntity.class);
        example.createCriteria().andIn("serveNo", serveNoList)
                .andEqualTo("status", ValidStatusEnum.VALID.getCode());
        return recoverVehicleMapper.selectByExample(example);
    }

    @Override
    public RecoverVehicleEntity getRecoverVehicleByDeliverNo(String deliverNo) {

        Example example = new Example(RecoverVehicleEntity.class);
        example.createCriteria().andEqualTo("deliverNo", deliverNo).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        return recoverVehicleMapper.selectOneByExample(example);
    }

    @Override
    public List<RecoverVehicleEntity> getRecoverVehicleByDeliverNoList(List<String> deliverNoList) {
        Example example = new Example(RecoverVehicleEntity.class);
        example.createCriteria()
                .andIn("deliverNo", deliverNoList);
        return recoverVehicleMapper.selectByExample(example);
    }

    @Override
    public List<RecoverVehicleEntity> getRecoverVehicleByDeliverNos(List<String> deliverNoList) {
        Example example = new Example(RecoverVehicleEntity.class);
        example.setOrderByClause("recover_vehicle_time DESC");
        example.createCriteria()
                .andIn("deliverNo", deliverNoList)
                .andIsNotNull("recoverVehicleTime");
        return recoverVehicleMapper.selectByExample(example);
    }

    @Override
    public int updateRecoverVehicleByDeliverNo(RecoverVehicleEntity recoverVehicle) {
        Example example = new Example(RecoverVehicleEntity.class);
        example.createCriteria().andEqualTo("deliverNo", recoverVehicle.getDeliverNo())
                .andEqualTo("status", ValidStatusEnum.VALID.getCode());;
        return recoverVehicleMapper.updateByExampleSelective(recoverVehicle, example);
    }


}
