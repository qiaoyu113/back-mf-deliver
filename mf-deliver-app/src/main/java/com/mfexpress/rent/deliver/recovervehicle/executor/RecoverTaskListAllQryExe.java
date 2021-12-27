package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverQryListCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverTaskListVO;
import com.mfexpress.rent.deliver.recovervehicle.RecoverQryServiceI;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Component
public class RecoverTaskListAllQryExe implements RecoverQryServiceI {
    @Resource
    private RecoverEsDataQryExe recoverEsDataQryExe;

    @Override
    public RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverStatus").gte(DeliverEnum.IS_RECOVER.getCode()));
        FieldSortBuilder sortSortBuilder = SortBuilders.fieldSort("sort").order(SortOrder.ASC);
        // 收车任务1.4迭代
        //    1. 第一排序规则：“待验车”＞“待收车”＞“签署中”＞“待退保”＞“待处理违章”＞“已完成”
        //    2. 第二排序规则：“最近一次数据更新时间”倒序
        //FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("expectRecoverTime").unmappedType("integer").order(SortOrder.ASC);
        FieldSortBuilder updateTimeSortBuilder = SortBuilders.fieldSort("updateTime").unmappedType("integer").order(SortOrder.DESC);

        List<FieldSortBuilder> fieldSortBuilderList = Arrays.asList(sortSortBuilder, updateTimeSortBuilder);
        return recoverEsDataQryExe.getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList, tokenInfo);
    }
}
