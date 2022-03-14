package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverQryListCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverTaskListVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleVO;
import com.mfexpress.rent.deliver.dto.entity.RecoverVehicle;
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
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RecoverTaskListDeductionQryExe implements RecoverQryServiceI {

    @Resource
    private RecoverEsDataQryExe recoverEsDataQryExe;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Override
    public RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverStatus").gte(DeliverEnum.RECOVER.getCode()))
                .must(QueryBuilders.matchQuery("isCheck", JudgeEnum.YES.getCode()))
                .must(QueryBuilders.matchQuery("isInsurance", JudgeEnum.YES.getCode()))
                .must(QueryBuilders.matchQuery("isDeduction", JudgeEnum.NO.getCode()));
        FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("recoverVehicleTime").unmappedType("integer").order(SortOrder.DESC);
        FieldSortBuilder updateTimeSortBuilder = SortBuilders.fieldSort("updateTime").unmappedType("integer").order(SortOrder.DESC);
        fieldSortBuilderList.add(timeSortBuilder);
        fieldSortBuilderList.add(updateTimeSortBuilder);
        RecoverTaskListVO esData = recoverEsDataQryExe.getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList, tokenInfo, Constants.ES_DELIVER_INDEX, Constants.ES_DELIVER_TYPE);
        List<RecoverVehicleVO> recoverVehicleVOList = esData.getRecoverVehicleVOList();
        if (!recoverVehicleVOList.isEmpty()) {
            List<String> serveNoList = recoverVehicleVOList.stream().map(RecoverVehicleVO::getServeNo).collect(Collectors.toList());
            Result<Map<String, RecoverVehicle>> recoverVehicleMapResult = recoverVehicleAggregateRootApi.getRecoverVehicleByServeNo(serveNoList);
            if (ResultErrorEnum.SUCCESSED.getCode() != recoverVehicleMapResult.getCode() || null == recoverVehicleMapResult.getData()) {
                return esData;
            }
            Map<String, RecoverVehicle> recoverVehicleMap = recoverVehicleMapResult.getData();
            recoverVehicleVOList.forEach(recoverVehicleVO -> {
                RecoverVehicle recoverVehicle = recoverVehicleMap.get(recoverVehicleVO.getServeNo());
                recoverVehicleVO.setDamageFee(recoverVehicle == null ? null : recoverVehicle.getDamageFee());
                recoverVehicleVO.setParkFee(recoverVehicle == null ? null : recoverVehicle.getParkFee());
                if (JudgeEnum.YES.getCode().equals(recoverVehicleVO.getRecoverAbnormalFlag())) {
                    recoverVehicleVO.setRecoverTypeDisplay(RecoverVehicleType.ABNORMAL.getName());
                } else {
                    recoverVehicleVO.setRecoverTypeDisplay(RecoverVehicleType.NORMAL.getName());
                }
            });
        }
        return esData;
    }
}
