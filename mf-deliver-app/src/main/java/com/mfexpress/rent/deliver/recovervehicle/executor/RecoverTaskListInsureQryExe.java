package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverQryListCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverTaskListVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
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
public class RecoverTaskListInsureQryExe implements RecoverQryServiceI {
    @Resource
    private RecoverEsDataQryExe recoverEsDataQryExe;
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Override
    public RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        boolQueryBuilder.must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.RECOVER.getCode()))
                .must(QueryBuilders.matchQuery("isCheck", JudgeEnum.YES.getCode()))
                /*.must(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("recoverContractStatus", DeliverContractStatusEnum.COMPLETED.getCode()))
                        .should(QueryBuilders.matchQuery("recoverAbnormalFlag", JudgeEnum.YES.getCode())))*/
                .must(QueryBuilders.matchQuery("isInsurance", JudgeEnum.NO.getCode()))
                .must(QueryBuilders.matchQuery("isDeduction", JudgeEnum.NO.getCode()));
        FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("recoverVehicleTime").unmappedType("integer").order(SortOrder.DESC);
        FieldSortBuilder updateTimeSortBuilder = SortBuilders.fieldSort("updateTime").unmappedType("integer").order(SortOrder.DESC);
        fieldSortBuilderList.add(timeSortBuilder);
        fieldSortBuilderList.add(updateTimeSortBuilder);
        RecoverTaskListVO recoverTaskListVO = recoverEsDataQryExe.getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList, tokenInfo, Constants.ES_DELIVER_INDEX, Constants.ES_DELIVER_TYPE);
        recoverTaskListVO.getRecoverVehicleVOList().forEach(recoverVehicleVO -> {
            Result<ServeDTO> serveDtoByServeNo = serveAggregateRootApi.getServeDtoByServeNo(recoverVehicleVO.getServeNo());

            recoverVehicleVO.setLeaseModelDisplay(LeaseModelEnum.getEnum(serveDtoByServeNo.getData().getLeaseModelId()).getName());
            recoverVehicleVO.setLeaseModelId(serveDtoByServeNo.getData().getLeaseModelId());
            if (JudgeEnum.YES.getCode().equals(recoverVehicleVO.getRecoverAbnormalFlag())) {
                recoverVehicleVO.setRecoverTypeDisplay(RecoverVehicleType.ABNORMAL.getName());
            } else {
                recoverVehicleVO.setRecoverTypeDisplay(RecoverVehicleType.NORMAL.getName());
            }
        });
        return recoverTaskListVO;

    }

}
