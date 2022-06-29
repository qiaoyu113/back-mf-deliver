package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.constant.*;
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
public class recoverTaskListWaitRecoverQryExe implements RecoverQryServiceI {

    @Resource
    private RecoverEsDataQryExe recoverEsDataQryExe;

    @Override
    public RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {
        // 1. 待收/待签 状态的交付单查询，交付单应符合的条件：
        //      serve.serverStatus:2 已发车，
        //      deliver.deliverStatus：3 收车中，
        //      deliver.isCheck：1 已验车，
        //      deliver.recover_contract_status：0 电子合同未签署
        //      deliver.is_insurance 0 是否退保未操作
        //      deliver.is_deduction 0 未进行处理违章操作
        // 2. 排序规则：“最近一次数据更新时间”倒序
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("serveStatus").gte(ServeEnum.DELIVER.getCode()))
                .must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.IS_RECOVER.getCode()))
                .must(QueryBuilders.matchQuery("isCheck", JudgeEnum.YES.getCode()))
                .must(QueryBuilders.matchQuery("recoverContractStatus", DeliverContractStatusEnum.NOSIGN.getCode()))
                .must(QueryBuilders.matchQuery("isInsurance", JudgeEnum.NO.getCode()))
                .must(QueryBuilders.matchQuery("isDeduction", JudgeEnum.NO.getCode()));
        // updateTime 指的是 deliver 的 updateTime
        FieldSortBuilder updateTimeSortBuilder = SortBuilders.fieldSort("updateTime").unmappedType("date").order(SortOrder.DESC);
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
