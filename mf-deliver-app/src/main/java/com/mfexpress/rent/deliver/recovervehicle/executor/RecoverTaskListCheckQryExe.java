package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.LeaseModelEnum;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverQryListCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverTaskListVO;
import com.mfexpress.rent.deliver.recovervehicle.RecoverEnum;
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
public class RecoverTaskListCheckQryExe implements RecoverQryServiceI {
    @Resource
    private RecoverEsDataQryExe recoverEsDataQryExe;

    @Override
    public RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        boolQueryBuilder.must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.IS_RECOVER.getCode()))
                .must(QueryBuilders.matchQuery("isCheck", JudgeEnum.NO.getCode()));
        FieldSortBuilder expectRecoverTimeSortBuilder = SortBuilders.fieldSort("expectRecoverTime").unmappedType("integer").order(SortOrder.DESC);
        FieldSortBuilder updateTimeSortBuilder = SortBuilders.fieldSort("updateTime").unmappedType("integer").order(SortOrder.DESC);

        fieldSortBuilderList.add(expectRecoverTimeSortBuilder);
        fieldSortBuilderList.add(updateTimeSortBuilder);
        RecoverTaskListVO esData = recoverEsDataQryExe.getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList, tokenInfo, Constants.ES_SERVE_INDEX, Constants.ES_SERVE_TYPE);
        esData.getRecoverVehicleVOList().forEach(recoverVehicleVO ->{
            if (recoverVehicleVO.getReplaceFlag().equals(1)){
                recoverVehicleVO.setLeaseModelDisplay(LeaseModelEnum.REPLACEMENT.getName());
                recoverVehicleVO.setLeaseModelId(LeaseModelEnum.REPLACEMENT.getCode());
            }

        });
        return esData;
    }
}
