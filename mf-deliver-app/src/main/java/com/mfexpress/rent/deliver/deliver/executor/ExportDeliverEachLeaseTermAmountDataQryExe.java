package com.mfexpress.rent.deliver.deliver.executor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryCmd;
import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeInfoVO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ExportDeliverEachLeaseTermAmountDataQryExe {

    @Resource
    private DeliverEachLeaseTermAmountQryExe qryExe;

    public List<Map<String, Object>> execute(Map<String, Object> map) {
        Map<String, Object> qryMap = (Map<String, Object>) map.get("qry");
        ServeQryCmd qry = BeanUtil.mapToBean(qryMap, ServeQryCmd.class, true, CopyOptions.create());
        ServeInfoVO serveInfoVO = qryExe.execute(qry);
        return serveInfoVO.getData().stream().map(BeanUtil::beanToMap).collect(Collectors.toList());
    }

}
