package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
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
import java.util.*;

@Component
public class RecoverTaskListAllQryExe implements RecoverQryServiceI {

    @Resource
    private RecoverEsDataQryExe recoverEsDataQryExe;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Override
    public RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverStatus").gte(DeliverEnum.IS_RECOVER.getCode()));

        FieldSortBuilder isCheckSortBuilder = SortBuilders.fieldSort("isCheck").order(SortOrder.ASC);

        FieldSortBuilder expectRecoverTimeBuilder = SortBuilders.fieldSort("expectRecoverTime").unmappedType("integer").order(SortOrder.DESC);
        FieldSortBuilder updateTimeSortBuilder = SortBuilders.fieldSort("updateTime").unmappedType("integer").order(SortOrder.DESC);

        List<FieldSortBuilder> fieldSortBuilderList = Arrays.asList(isCheckSortBuilder, expectRecoverTimeBuilder, updateTimeSortBuilder);
        RecoverTaskListVO esData = recoverEsDataQryExe.getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList, tokenInfo);

        List<RecoverVehicleVO> recoverVehicleVOList = esData.getRecoverVehicleVOList();
        if (!recoverVehicleVOList.isEmpty()) {
            List<String> needSupplementFeeServeNoList = new ArrayList<>();
            recoverVehicleVOList.forEach(recoverVehicleVO -> {
                if(Objects.equals(DeliverEnum.RECOVER.getCode(), recoverVehicleVO.getDeliverStatus()) && Objects.equals(JudgeEnum.NO.getCode(), recoverVehicleVO.getIsDeduction())){
                    needSupplementFeeServeNoList.add(recoverVehicleVO.getServeNo());
                }
            });
            if(!needSupplementFeeServeNoList.isEmpty()){
                Result<Map<String, RecoverVehicle>> recoverVehicleMapResult = recoverVehicleAggregateRootApi.getRecoverVehicleByServeNo(needSupplementFeeServeNoList);
                if (ResultErrorEnum.SUCCESSED.getCode() != recoverVehicleMapResult.getCode() || null == recoverVehicleMapResult.getData()) {
                    return esData;
                }
                Map<String, RecoverVehicle> recoverVehicleMap = recoverVehicleMapResult.getData();
                recoverVehicleVOList.forEach(recoverVehicleVO -> {
                    RecoverVehicle recoverVehicle = recoverVehicleMap.get(recoverVehicleVO.getServeNo());
                    if(Objects.equals(DeliverEnum.RECOVER.getCode(), recoverVehicleVO.getDeliverStatus()) && Objects.equals(JudgeEnum.NO.getCode(), recoverVehicleVO.getIsDeduction())){
                        recoverVehicleVO.setDamageFee(recoverVehicle == null ? null : recoverVehicle.getDamageFee());
                        recoverVehicleVO.setParkFee(recoverVehicle == null ? null : recoverVehicle.getParkFee());
                    }
                });
            }

        }
        return esData;
    }
}
