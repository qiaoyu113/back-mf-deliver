package com.mfexpress.rent.deliver.serve;

import com.github.pagehelper.PageHelper;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.dto.data.serve.ServeListQry;
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
    public List<Serve> getCycleServe(List<Integer> customerIdList) {
        Example example = new Example(Serve.class);
        if (customerIdList != null && customerIdList.size() > 0) {
            example.createCriteria().andGreaterThanOrEqualTo("status", ServeEnum.DELIVER.getCode()).andIn("customerId", customerIdList);
        } else {
            example.createCriteria().andGreaterThanOrEqualTo("status", ServeEnum.DELIVER.getCode());

        }


        return serveMapper.selectByExample(example);


    }

    @Override
    public List<Serve> getServeByServeNoList(List<String> serveNoList) {
        Example example = new Example(Serve.class);
        example.createCriteria().andIn("serveNo", serveNoList);
        return serveMapper.selectByExample(example);
    }

    @Override
    public List<Serve> getServeListByOrderIds(List<Long> orderIds) {
        Example example = new Example(Serve.class);
        example.createCriteria()
                .andIn("orderId", orderIds);
        return serveMapper.selectByExample(example);
    }

    @Override
    public Integer getCountByQry(ServeListQry qry) {
        Example example = new Example(Serve.class);
        example.createCriteria().andIn("status", qry.getStatuses());
        return serveMapper.selectCountByExample(example);
    }

    @Override
    public PagePagination<Serve> getPageServeByQry(ServeListQry qry) {
        if(qry.getPage() == 0){
            qry.setPage(1);
        }
        if(qry.getLimit() == 0){
            qry.setLimit(5);
        }
        PageHelper.clearPage();
        PageHelper.startPage(qry.getPage(), qry.getLimit());

        Example example = new Example(Serve.class);
        example.createCriteria().andIn("status", qry.getStatuses());
        List<Serve> serveList = serveMapper.selectByExample(example);
        return PagePagination.getInstance(serveList);
    }

    @Override
    public void batchUpdate(List<Serve> serveToUpdateList) {
        serveToUpdateList.forEach(serve -> {
            serveMapper.updateByPrimaryKeySelective(serve);
        });
    }

    @Override
    public List<Serve> getServeByCustomerIdDeliver(List<Integer> customerIdList) {
        Example example = new Example(Serve.class);
        Example.Criteria criteria1 = example.createCriteria();
        Example.Criteria criteria2 = example.createCriteria();

        criteria1 .andIn("customerId", customerIdList);
        criteria2.orEqualTo("status",2).andEqualTo("status",5);
        example.and(criteria2);
        return serveMapper.selectByExample(example);
    }

}
