package com.mfexpress.rent.deliver.serve.executor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.order.dto.qry.OrderListVOQry;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAllLeaseTermAmountVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeLeaseTermAmountQry;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ExportServeLeaseTermAmountDataQryExe {

    @Resource
    private ServeLeaseTermAmountQryExe serveLeaseTermAmountQryExe;

    public List<Map<String, Object>> execute(Map<String, Object> map) {
        Map<String, Object> qryMap = (Map<String, Object>) map.get("qry");
        ServeLeaseTermAmountQry qry = BeanUtil.mapToBean(qryMap, ServeLeaseTermAmountQry.class, true, CopyOptions.create());
        List<ServeAllLeaseTermAmountVO> result = new ArrayList<>();
        // 一次1000条
        qry.setLimit(1000);
        for (int i = 1; ; i++) {
            qry.setPage(i);
            PagePagination<ServeAllLeaseTermAmountVO> pagePagination = serveLeaseTermAmountQryExe.execute(qry, null);
            List<ServeAllLeaseTermAmountVO> list = pagePagination.getList();
            result.addAll(list);

            int totalPages = pagePagination.getPagination().getTotalPages();
            if(i >= totalPages){
                break;
            }
        }
        return result.stream().map(BeanUtil::beanToMap).collect(Collectors.toList());
    }
}
