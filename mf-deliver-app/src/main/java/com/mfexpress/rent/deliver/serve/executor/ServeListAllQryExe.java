package com.mfexpress.rent.deliver.serve.executor;


import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.constant.DeliverContractStatusEnum;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.DeliverTypeEnum;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.qry.ContractListQry;
import com.mfexpress.rent.deliver.dto.data.serve.ServeListVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryListCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeVO;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ServeListAllQryExe {

    @Resource
    private ServeEsDataQryExe serveEsDataQryExe;

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    public ServeListVO execute(ServeQryListCmd serveQryListCmd) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("orderId", serveQryListCmd.getOrderId()))
                .must(QueryBuilders.rangeQuery("deliverStatus").lte(DeliverEnum.DELIVER.getCode()));
        FieldSortBuilder sortSortBuilders = SortBuilders.fieldSort("sort").order(SortOrder.ASC);
        FieldSortBuilder updateTimeSortBuilders = SortBuilders.fieldSort("updateTime").unmappedType("integer").order(SortOrder.DESC);
        List<FieldSortBuilder> fieldSortBuilders = Arrays.asList(sortSortBuilders, updateTimeSortBuilders);

        ServeListVO serveListVO = serveEsDataQryExe.execute(serveQryListCmd.getOrderId(), boolQueryBuilder, serveQryListCmd.getPage(), serveQryListCmd.getLimit(), fieldSortBuilders);
        List<ServeVO> serveVOList = serveListVO.getServeVOList();
        List<String> deliverNos = new ArrayList<>();
        serveVOList.forEach(serveVO -> {
            if (null != serveVO.getDeliverContractStatus() && (DeliverContractStatusEnum.GENERATING.getCode() == serveVO.getDeliverContractStatus() || DeliverContractStatusEnum.SIGNING.getCode() == serveVO.getDeliverContractStatus())) {
                deliverNos.add(serveVO.getDeliverNo());
            }
        });
        if(!deliverNos.isEmpty()){
            ContractListQry contractListQry = new ContractListQry();
            contractListQry.setDeliverNos(deliverNos);
            contractListQry.setDeliverType(DeliverTypeEnum.DELIVER.getCode());
            contractListQry.setOrderId(serveListVO.getServeVOList().get(0).getOrderId());
            Result<Map<String, String>> mapResult = contractAggregateRootApi.getContractIdMapByQry(contractListQry);
            if(ResultErrorEnum.SUCCESSED.getCode() == mapResult.getCode() && null != mapResult.getData()){
                Map<String, String> contractIdMap = mapResult.getData();
                serveVOList.forEach(serveVO -> {
                    serveVO.setElecContractId(contractIdMap.get(serveVO.getDeliverNo()));
                });
            }
        }
        return serveListVO;
    }

}
