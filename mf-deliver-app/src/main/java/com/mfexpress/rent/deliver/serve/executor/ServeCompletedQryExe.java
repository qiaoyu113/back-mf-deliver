package com.mfexpress.rent.deliver.serve.executor;

import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.dto.data.serve.ServeListVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryListCmd;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Component
public class ServeCompletedQryExe {
    @Resource
    private ServeEsDataQryExe serveEsDataQryExe;

    public ServeListVO execute(ServeQryListCmd serveQryListCmd) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("orderId", serveQryListCmd.getOrderId()))
                .must(QueryBuilders.rangeQuery("deliverStatus").gte(DeliverEnum.DELIVER.getCode()));
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        FieldSortBuilder updateTimeSortBuilders = SortBuilders.fieldSort("updateTime").unmappedType("integer").order(SortOrder.DESC);
        fieldSortBuilderList.add(updateTimeSortBuilders);

        return serveEsDataQryExe.execute(serveQryListCmd.getOrderId(), boolQueryBuilder, serveQryListCmd.getPage(), serveQryListCmd.getLimit(), fieldSortBuilderList);
    }
}
