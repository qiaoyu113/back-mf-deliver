package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.RecoverVehicleType;
import com.mfexpress.rent.deliver.constant.ServeEnum;
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
public class RecoverTaskListCompletedQryExe implements RecoverQryServiceI {
    @Resource
    private RecoverEsDataQryExe recoverEsDataQryExe;

    @Override
    public RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        boolQueryBuilder.must(QueryBuilders.matchQuery("serveStatus", ServeEnum.COMPLETED.getCode()));
        FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("recoverVehicleTime").unmappedType("integer").order(SortOrder.DESC);
        FieldSortBuilder updateTimeSortBuilder = SortBuilders.fieldSort("updateTime").unmappedType("integer").order(SortOrder.DESC);

        fieldSortBuilderList.add(timeSortBuilder);
        fieldSortBuilderList.add(updateTimeSortBuilder);
        RecoverTaskListVO recoverTaskListVO = recoverEsDataQryExe.getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList, tokenInfo, Constants.ES_DELIVER_INDEX, Constants.ES_DELIVER_TYPE);
        recoverTaskListVO.getRecoverVehicleVOList().forEach(recoverVehicleVO -> {
            if (JudgeEnum.YES.getCode().equals(recoverVehicleVO.getRecoverAbnormalFlag())) {
                recoverVehicleVO.setRecoverTypeDisplay(RecoverVehicleType.ABNORMAL.getName());
            } else {
                recoverVehicleVO.setRecoverTypeDisplay(RecoverVehicleType.NORMAL.getName());
            }
        });
        return recoverTaskListVO;
    }
}
