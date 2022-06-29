package com.mfexpress.rent.deliver.gateway;

import com.mfexpress.rent.deliver.po.ServeAdjustPO;
import tk.mybatis.mapper.entity.Example;

public interface ServeAdjustGateway {

    int save(ServeAdjustPO po);

    int updateByServeNo(ServeAdjustPO po);

    ServeAdjustPO getByServeNo(String serveNo);

    default Example getServeNoExample(String serveNo) {

        Example example = new Example(ServeAdjustPO.class);
        example.createCriteria().andEqualTo("serveNo", serveNo);

        return example;
    }
}
