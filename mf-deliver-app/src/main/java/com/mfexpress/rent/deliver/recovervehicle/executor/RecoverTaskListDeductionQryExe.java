package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
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
import java.util.LinkedList;
import java.util.List;

@Component
public class RecoverTaskListDeductionQryExe implements RecoverQryServiceI {
    @Resource
    private RecoverEsDataQryExe recoverEsDataQryExe;

    @Override
    public RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        boolQueryBuilder.must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.IS_RECOVER.getCode()))
                .must(QueryBuilders.matchQuery("isCheck", JudgeEnum.YES.getCode()))
                .must(QueryBuilders.matchQuery("isDeduction", JudgeEnum.NO.getCode()));
        FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("recoverVehicleTime").unmappedType("integer").order(SortOrder.DESC);
        fieldSortBuilderList.add(timeSortBuilder);
        return recoverEsDataQryExe.getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList);
    }
}
