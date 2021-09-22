package com.mfexpress.rent.deliver.serve.repository;

import com.mfexpress.rent.deliver.base.BaseMapper;
import com.mfexpress.rent.deliver.dto.data.serve.ServePreselectedDTO;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ServeMapper extends BaseMapper<Serve> {


    @Select("<script>" +
            "select order_id as orderId, count(status=0 OR NULL)as notPreselectedNum,count(status>0 OR NULL) as isPreselectedNum  " +
            "from serve where order_id in ("
            + "<foreach collection='orderIdList' item='orderId' index='index' separator=','>"
            + "#{orderId}"
            + "</foreach>"
            + ") " +
            " GROUP BY order_id"
            + "</script>")
    List<ServePreselectedDTO> getServePreselectedByOrderId(@Param("orderIdList") List<Long> orderIdList);
}
