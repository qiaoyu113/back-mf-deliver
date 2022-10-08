package com.mfexpress.rent.deliver.serve;

import cn.hutool.core.collection.CollectionUtil;
import com.github.pagehelper.PageHelper;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.dto.data.ListQry;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositListDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeListQry;
import com.mfexpress.rent.deliver.dto.data.serve.ServePreselectedDTO;
import com.mfexpress.rent.deliver.entity.ServeEntity;
import com.mfexpress.rent.deliver.gateway.ServeGateway;
import com.mfexpress.rent.deliver.serve.repository.ServeMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ServeGatewayImpl implements ServeGateway {

    @Resource
    private ServeMapper serveMapper;

    @Override
    public int updateServeByServeNo(String serveNo, ServeEntity serve) {
        Example example = new Example(ServeEntity.class);
        example.createCriteria().andEqualTo("serveNo", serveNo);
        return serveMapper.updateByExampleSelective(serve, example);
    }

    @Override
    public int updateServeByServeNoList(List<String> serveNoList, ServeEntity serve) {
        Example example = new Example(ServeEntity.class);
        example.createCriteria().andIn("serveNo", serveNoList);
        return serveMapper.updateByExampleSelective(serve, example);

    }

    @Override
    public ServeEntity getServeByServeNo(String serveNo) {
        Example example = new Example(ServeEntity.class);
        example.createCriteria().andEqualTo("serveNo", serveNo);

        return serveMapper.selectOneByExample(example);
    }

    @Override
    public void addServeList(List<ServeEntity> serveList) {
        serveMapper.insertList(serveList);
    }

    @Override
    public List<ServePreselectedDTO> getServePreselectedByOrderId(List<Long> orderId) {


        return serveMapper.getServePreselectedByOrderId(orderId);
    }

    @Override
    public List<String> getServeNoListAll() {

        return serveMapper.selectAll().stream().map(ServeEntity::getServeNo).collect(Collectors.toList());
    }

    @Override
    public List<ServeEntity> getServeByStatus() {
        Example example = new Example(ServeEntity.class);
        example.createCriteria().orEqualTo("status", ServeEnum.DELIVER.getCode()).orEqualTo("status", ServeEnum.REPAIR.getCode());
        return serveMapper.selectByExample(example);


    }

    @Override
    public List<ServeEntity> getCycleServe(List<Integer> customerIdList) {
        Example example = new Example(ServeEntity.class);
        if (customerIdList != null && customerIdList.size() > 0) {
            example.createCriteria().andGreaterThanOrEqualTo("status", ServeEnum.DELIVER.getCode()).andIn("customerId", customerIdList);
        } else {
            example.createCriteria().andGreaterThanOrEqualTo("status", ServeEnum.DELIVER.getCode());

        }


        return serveMapper.selectByExample(example);


    }

    @Override
    public List<ServeEntity> getServeByServeNoList(List<String> serveNoList) {
        Example example = new Example(ServeEntity.class);
        example.createCriteria().andIn("serveNo", serveNoList);
        return serveMapper.selectByExample(example);
    }

    @Override
    public List<ServeEntity> getServeListByOrderIds(List<Long> orderIds) {
        Example example = new Example(ServeEntity.class);
        example.createCriteria()
                .andIn("orderId", orderIds);
        return serveMapper.selectByExample(example);
    }

    @Override
    public Integer getCountByQry(ServeListQry qry) {
        Example example = new Example(ServeEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if (null != qry.getReplaceFlag()) {
            criteria.andEqualTo("replaceFlag", qry.getReplaceFlag());
        }
        criteria.andIn("status", qry.getStatuses());
        return serveMapper.selectCountByExample(example);
    }

    @Override
    public PagePagination<ServeEntity> getPageServeByQry(ServeListQry qry) {
        if (qry.getPage() == 0) {
            qry.setPage(1);
        }
        if (qry.getLimit() == 0) {
            qry.setLimit(5);
        }
        PageHelper.clearPage();
        PageHelper.startPage(qry.getPage(), qry.getLimit());

        Example example = new Example(ServeEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if (null != qry.getReplaceFlag()) {
            criteria.andEqualTo("replaceFlag", qry.getReplaceFlag());
        }

        criteria.andIn("status", qry.getStatuses());
        List<ServeEntity> serveList = serveMapper.selectByExample(example);
        return PagePagination.getInstance(serveList);
    }

    @Override
    public void batchUpdate(List<ServeEntity> serveToUpdateList) {
        serveToUpdateList.forEach(serve -> {
            serveMapper.updateByPrimaryKeySelective(serve);
        });
    }

    @Override
    public List<ServeEntity> getServeByCustomerIdDeliver(List<Integer> customerIdList) {
        Example example = new Example(ServeEntity.class);
        Example.Criteria criteria1 = example.createCriteria();
        Example.Criteria criteria2 = example.createCriteria();

        criteria1.andIn("customerId", customerIdList);
        criteria2.orEqualTo("status", 2).orEqualTo("status", 5);
        example.and(criteria2);
        return serveMapper.selectByExample(example);
    }

    @Override
    public List<ServeEntity> getServeByCustomerIdRecover(List<Integer> customerIdList) {
        Example example = new Example(ServeEntity.class);
        Example.Criteria criteria1 = example.createCriteria();
        Example.Criteria criteria2 = example.createCriteria();

        criteria1.andIn("customerId", customerIdList);
        criteria2.orEqualTo("status", 3).orEqualTo("status", 4);
        example.and(criteria2);
        return serveMapper.selectByExample(example);
    }

    @Override
    public PagePagination<ServeEntity> getServeNoListByPage(ListQry qry) {
        if(qry.getPage() == 0){
            qry.setPage(1);
        }
        if(qry.getLimit() == 0){
            qry.setLimit(5);
        }
        PageHelper.clearPage();
        PageHelper.startPage(qry.getPage(), qry.getLimit());

        Example example = new Example(ServeEntity.class);
        // example.createCriteria().andEqualTo("status", 5);
        example.selectProperties("serveNo");

        List<ServeEntity> serveEntityList = serveMapper.selectByExample(example);
        return PagePagination.getInstance(serveEntityList);
    }

    @Override
    public ServeEntity getServeDepositByServeNo(CustomerDepositListDTO qry) {
        Example example = new Example(ServeEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if (CollectionUtil.isNotEmpty(qry.getOrgIdList())) {
            criteria.andIn("orgId", qry.getOrgIdList());
        }
        if (Objects.nonNull(qry.getCustomerId()) && qry.getCustomerId() != 0) {
            criteria.andEqualTo("customerId", qry.getCustomerId());
        }
        if (Objects.nonNull(qry.getHasPaidDeposit()) && !qry.getHasPaidDeposit()) {
            criteria.andEqualTo("paidInDeposit", 0);
        } else if (Objects.nonNull(qry.getHasPaidDeposit()) && qry.getHasPaidDeposit()) {
            criteria.andGreaterThan("paidInDeposit", 0);
        }
        if (CollectionUtil.isNotEmpty(qry.getStatusList())) {
            criteria.andIn("status", qry.getStatusList());
        }
        criteria.andEqualTo("serveNo", qry.getServeNo());
        return serveMapper.selectOneByExample(example);
    }

    @Override
    public PagePagination<ServeEntity> pageServeDeposit(CustomerDepositListDTO qry) {
        PageHelper.startPage(qry.getPage(), qry.getLimit());
        Example example = new Example(ServeEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if (CollectionUtil.isNotEmpty(qry.getOrgIdList())) {
            criteria.andIn("orgId", qry.getOrgIdList());
        }
        if (Objects.nonNull(qry.getCustomerId()) && qry.getCustomerId() != 0) {
            criteria.andEqualTo("customerId", qry.getCustomerId());
        }
        if (Objects.nonNull(qry.getHasPaidDeposit()) && !qry.getHasPaidDeposit()) {
            criteria.andEqualTo("paidInDeposit", 0);
        } else if (Objects.nonNull(qry.getHasPaidDeposit()) && qry.getHasPaidDeposit()) {
            criteria.andGreaterThan("paidInDeposit", 0);
        }
        if (CollectionUtil.isNotEmpty(qry.getStatusList())) {
            criteria.andIn("status", qry.getStatusList());
        }
        example.setOrderByClause("create_time DESC,paid_in_deposit DESC");
        List<ServeEntity> serveEntityList = serveMapper.selectByExample(example);
        return PagePagination.getInstance(serveEntityList);
    }

    @Override
    public Map<Integer, Integer> getReplaceNumByCustomerIds(List<Integer> customerIds) {
        Example example = new Example(ServeEntity.class);
        example.createCriteria().andEqualTo("replaceFlag",1).andEqualTo("status", DeliverEnum.DELIVER.getCode()).andIn("customerId",customerIds);
        List<ServeEntity> serveEntities = serveMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(serveEntities)){
            return new HashMap<>();
        }

        Map<Integer, List<ServeEntity>> integerListMap = serveEntities.stream().collect(Collectors.groupingBy(
                ServeEntity::getCustomerId));

        Map<Integer, Integer> mapAll = new HashMap<>();

        for (Map.Entry<Integer, List<ServeEntity>> map : integerListMap.entrySet()){
            mapAll.put(map.getKey(),map.getValue().size());
        }

        return mapAll;
    }

    @Override
    public Integer getRentingServeNumByCustomerId(Integer customerId) {
        Example example = new Example(ServeEntity.class);
        example.createCriteria().andIn("status", Arrays.asList(ServeEnum.DELIVER.getCode(), ServeEnum.REPAIR.getCode()))
                .andEqualTo("customerId", customerId);
        return serveMapper.selectCountByExample(example);
    }

    @Override
    public int updateServe(ServeEntity serveEntity) {
        Example example = new Example(ServeEntity.class);
        example.createCriteria()
                .andEqualTo("serveNo", serveEntity.getServeNo());
        return serveMapper.updateByExampleSelective(serveEntity, example);
    }

    /*@Override
    public Integer updateServePayableDepositByContractCommodityId(ServeEntity serveEntity) {
        Example example = new Example(ServeEntity.class);
        example.createCriteria().andEqualTo("serveNo", serveEntity.getServeNo());

        return serveMapper.updateByExampleSelective(serveEntity, example);
    }*/


}
