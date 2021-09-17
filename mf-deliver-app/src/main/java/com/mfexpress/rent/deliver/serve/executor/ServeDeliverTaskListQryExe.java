package com.mfexpress.rent.deliver.serve.executor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.mfexpress.component.starter.utils.ElasticsearchTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.dto.data.Page;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDeliverTaskListVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDeliverTaskQryCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDeliverTaskVO;
import com.mfexpress.rent.deliver.dto.es.ServeES;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ServeDeliverTaskListQryExe {

    @Resource
    private ElasticsearchTools elasticsearchTools;

    public ServeDeliverTaskListVO execute(ServeDeliverTaskQryCmd serveDeliverTaskQryCmd) {
        ServeDeliverTaskListVO serveDeliverTaskListVO = new ServeDeliverTaskListVO();
        List<ServeDeliverTaskVO> serveDeliverTaskVOList = new LinkedList<>();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(serveDeliverTaskQryCmd.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("customerName", serveDeliverTaskQryCmd.getKeyword()));
        }
        if (serveDeliverTaskQryCmd.getTag() == 1) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverStatus").lt(DeliverEnum.DELIVER.getCode()));

        } else if (serveDeliverTaskQryCmd.getTag() == 2) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.DELIVER.getCode()));
        }
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("extractVehicleTime").unmappedType("integer").order(SortOrder.ASC);
        fieldSortBuilderList.add(timeSortBuilder);
        //查询所有服务单
        Map<String, Object> map = elasticsearchTools.searchByQuerySort(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX),
                DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), 0, 0,
                boolQueryBuilder, fieldSortBuilderList);
        List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
        List<ServeES> serveEsList = new LinkedList<>();
        for (Map<String, Object> serveMap : data) {
            ServeES serveEs = BeanUtil.mapToBean(serveMap, ServeES.class, false, new CopyOptions());

            serveEsList.add(serveEs);
        }
        //手动分页

        Map<String, List<ServeES>> aggMap = serveEsList.stream().collect(Collectors.groupingBy(ServeES::getOrderId));
        for (String orderId : aggMap.keySet()) {
            ServeDeliverTaskVO serveDeliverTaskVO = new ServeDeliverTaskVO();
            serveDeliverTaskVO.setOrderId(orderId);
            serveDeliverTaskVO.setCarModelVOList(aggMap.get(orderId).get(0).getCarModelVOList());
            serveDeliverTaskVO.setCustomerName(aggMap.get(orderId).get(0).getCustomerName());
            serveDeliverTaskVO.setExtractVehicleTime(aggMap.get(orderId).get(0).getExtractVehicleTime());
            serveDeliverTaskVO.setStayDeliverNum(aggMap.get(orderId).size());
            serveDeliverTaskVOList.add(serveDeliverTaskVO);
        }
        //总条数
        int total = serveDeliverTaskVOList.size();
        BigDecimal bigDecimalTotal = new BigDecimal(total);
        BigDecimal bigDecimalLimit = new BigDecimal(serveDeliverTaskQryCmd.getLimit());
        BigDecimal pages = bigDecimalTotal.divide(bigDecimalLimit, BigDecimal.ROUND_UP);
        Page page = Page.builder().nowPage(serveDeliverTaskQryCmd.getPage()).pages(pages.intValue()).total(total).build();
        int start = (serveDeliverTaskQryCmd.getPage() - 1) * serveDeliverTaskQryCmd.getLimit();

        if (start > total) {
            serveDeliverTaskListVO.setServeDeliverTaskVOList(serveDeliverTaskVOList);
        } else if (start + serveDeliverTaskQryCmd.getLimit() > total) {
            serveDeliverTaskListVO.setServeDeliverTaskVOList(serveDeliverTaskVOList.subList(start, serveDeliverTaskVOList.size()));
        } else {
            serveDeliverTaskListVO.setServeDeliverTaskVOList(serveDeliverTaskVOList.subList(start, start + serveDeliverTaskQryCmd.getLimit()));
        }
        serveDeliverTaskListVO.setPage(page);

        return serveDeliverTaskListVO;
    }
}
