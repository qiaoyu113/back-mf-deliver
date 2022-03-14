package com.mfexpress.rent.deliver.deliver;

import com.github.pagehelper.PageHelper;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.DeliverStatusEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.deliver.repository.DeliverMapper;
import com.mfexpress.rent.deliver.dto.data.ListQry;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverQry;
import com.mfexpress.rent.deliver.dto.entity.Deliver;
import com.mfexpress.rent.deliver.entity.DeliverEntity;
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
    public int addDeliver(List<DeliverEntity> deliverList) {
        int i = 0;
        for (DeliverEntity deliver : deliverList) {
            i += deliverMapper.insertSelective(deliver);
        }
        return i;
    }

    @Override
    public int updateDeliverByServeNo(String serveNo, DeliverEntity deliver) {
        Example example = new Example(DeliverEntity.class);
        example.createCriteria().andEqualTo("serveNo", serveNo).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        int i = deliverMapper.updateByExampleSelective(deliver, example);
        return i;
    }

    @Override
    public int updateDeliverByServeNoList(List<String> serveNoList, DeliverEntity deliver) {
        Example example = new Example(DeliverEntity.class);
        example.createCriteria().andIn("serveNo", serveNoList).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        int i = deliverMapper.updateByExampleSelective(deliver, example);
        return i;
    }


    @Override
    public DeliverEntity getDeliverByServeNo(String serveNo) {
        Example example = new Example(DeliverEntity.class);
        example.createCriteria().andEqualTo("serveNo", serveNo).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        return deliverMapper.selectOneByExample(example);

    }

    @Override
    public DeliverEntity getDeliverByCarIdAndDeliverStatus(Integer carId, List<Integer> deliverStatus) {
        Example example = new Example(DeliverEntity.class);
        example.createCriteria().andEqualTo("carId", carId).andEqualTo("status", ValidStatusEnum.VALID.getCode())
                .andIn("deliverStatus", deliverStatus);
        return deliverMapper.selectOneByExample(example);
    }

    @Override
    public int updateInsuranceStatusByCarId(List<Integer> carId, Integer status1, Integer status2) {
        Example example = new Example(DeliverEntity.class);
        //发车中交付单
        example.createCriteria().andIn("carId", carId).andEqualTo("deliverStatus", DeliverEnum.IS_DELIVER.getCode());
        DeliverEntity deliver = DeliverEntity.builder().isInsurance(status1).build();
        int i = deliverMapper.updateByExampleSelective(deliver, example);
        Example example1 = new Example(DeliverEntity.class);
        //收车中交付单
        example1.createCriteria().andIn("carId", carId).andNotEqualTo("deliverStatus", DeliverEnum.IS_DELIVER.getCode());
        DeliverEntity deliver1 = DeliverEntity.builder().isInsurance(status2).build();
        int j = deliverMapper.updateByExampleSelective(deliver1, example1);
        return i + j;
    }

    @Override
    public int updateMileageAndVehicleAgeByCarId(Integer carId, DeliverEntity deliver) {

        Example example = new Example(DeliverEntity.class);
        example.createCriteria().andEqualTo("carId", carId);
        return deliverMapper.updateByExampleSelective(deliver, example);
    }

    @Override
    public List<DeliverEntity> getDeliverDeductionByServeNoList(List<String> serveNoList) {
        //查询已经处理违章的交付单
        Example example = new Example(DeliverEntity.class);
        example.createCriteria().andIn("serveNo", serveNoList).andEqualTo("isDeduction", JudgeEnum.YES.getCode());
        return deliverMapper.selectByExample(example);
    }

    @Override
    public List<DeliverEntity> getDeliverByServeNoList(List<String> serveNoList) {
        Example example = new Example(DeliverEntity.class);
        example.createCriteria().andIn("serveNo", serveNoList).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        return deliverMapper.selectByExample(example);
    }

    @Override
    public List<DeliverEntity> getDeliverByDeliverNoList(List<String> deliverNos) {
        Example example = new Example(DeliverEntity.class);
        example.createCriteria().andIn("deliverNo", deliverNos).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        return deliverMapper.selectByExample(example);
    }

    @Override
    public List<DeliverEntity> getDeliverByDeductStatus(List<String> serveNoList) {
        Example example = new Example(DeliverEntity.class);
        if (serveNoList != null && serveNoList.size() > 0) {
            example.createCriteria().andNotIn("serveNo", serveNoList).andEqualTo("isDeduction", JudgeEnum.YES.getCode()).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        } else {
            example.createCriteria().andEqualTo("isDeduction", JudgeEnum.YES.getCode()).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        }
        return deliverMapper.selectByExample(example);
    }

    @Override
    public int updateDeliverByDeliverNos(List<String> deliverNos, DeliverEntity deliver) {
        Example example = new Example(Deliver.class);
        example.createCriteria().andIn("deliverNo", deliverNos).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        return deliverMapper.updateByExampleSelective(deliver, example);
    }

    @Override
    public DeliverEntity getDeliverByDeliverNo(String deliverNo) {
        Example example = new Example(DeliverEntity.class);
        example.createCriteria().andEqualTo("deliverNo", deliverNo).andEqualTo("status", ValidStatusEnum.VALID.getCode());
        return deliverMapper.selectOneByExample(example);
    }

    @Override
    public List<DeliverEntity> getDeliverByCarId(Integer carId) {
        Example example = new Example(DeliverEntity.class);
        example.createCriteria()
                .andEqualTo("carId",carId);
        example.setOrderByClause("create_time desc");
        return deliverMapper.selectByCondition(example);


    }

    @Override
    public List<DeliverEntity> getDeliverDTOSByCarIdList(List<Integer> carIds) {
        Example example = new Example(DeliverEntity.class);
        example.createCriteria()
                .andIn("carId", carIds);
        return deliverMapper.selectByExample(example);
    }

    @Override
    public int updateDeliverByDeliverNo(String deliverNo, DeliverEntity deliverEntity) {
        Example example = new Example(DeliverEntity.class);
        example.createCriteria().andEqualTo("deliverNo", deliverNo);
        return deliverMapper.updateByExampleSelective(deliverEntity, example);
    }

    @Override
    public List<DeliverEntity> getHistoryListByServeNoList(List<String> reactiveServeNoList) {
        Example example = new Example(DeliverEntity.class);
        example.createCriteria().andIn("serveNo", reactiveServeNoList)
                .andEqualTo("status", DeliverStatusEnum.HISTORICAL.getCode());
        example.orderBy("createTime");

        return deliverMapper.selectByExample(example);
    }

    @Override
    public List<DeliverEntity> getDeliverNoListByPage(DeliverQry qry) {
        if(qry.getPage() == 0){
            qry.setPage(1);
        }
        if(qry.getLimit() == 0){
            qry.setLimit(5);
        }
        PageHelper.clearPage();
        PageHelper.startPage(qry.getPage(), qry.getLimit());

        Example example = new Example(DeliverEntity.class);
        Example.Criteria criteria = example.createCriteria().andNotEqualTo("status", DeliverStatusEnum.INVALID.getCode());
                // .andEqualTo("carServiceId", 36);
        if(null != qry.getDeliverStatus() && !qry.getDeliverStatus().isEmpty()){
            criteria.andIn("deliverStatus", qry.getDeliverStatus());
        }
        example.selectProperties("deliverNo");

        return deliverMapper.selectByExample(example);
    }

}
