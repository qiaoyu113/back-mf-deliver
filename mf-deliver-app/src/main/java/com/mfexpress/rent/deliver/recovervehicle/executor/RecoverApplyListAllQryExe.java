package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.api.SyncServiceI;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverQryListCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverTaskListVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleVO;
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
import java.util.stream.Collectors;

@Component
public class RecoverApplyListAllQryExe implements RecoverQryServiceI {
    @Resource
    private RecoverEsDataQryExe recoverEsDataQryExe;
    @Resource
    private SyncServiceI syncServiceI;


    @Override
    public RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverStatus").gte(DeliverEnum.IS_RECOVER.getCode()));
        FieldSortBuilder checkSortBuilder = SortBuilders.fieldSort("isCheck").order(SortOrder.ASC);
        FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("expectRecoverTime").unmappedType("integer").order(SortOrder.ASC);
        List<FieldSortBuilder> fieldSortBuilderList = Arrays.asList(checkSortBuilder, timeSortBuilder);
        RecoverTaskListVO recoverTaskListVO = recoverEsDataQryExe.getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList, tokenInfo);
        //暂时强同步 后续处理
        if (recoverTaskListVO != null && recoverTaskListVO.getRecoverVehicleVOList() != null) {
            List<String> serveNoList = recoverTaskListVO.getRecoverVehicleVOList().stream().map(RecoverVehicleVO::getServeNo).collect(Collectors.toList());
            for (String serveNo : serveNoList) {
                syncServiceI.execOne(serveNo);
            }
        }
        return recoverEsDataQryExe.getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList, tokenInfo);
    }
}
