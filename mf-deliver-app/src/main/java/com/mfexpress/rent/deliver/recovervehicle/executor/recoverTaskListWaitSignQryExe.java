package com.mfexpress.rent.deliver.recovervehicle.executor;

import cn.hutool.json.JSONUtil;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class recoverTaskListWaitSignQryExe implements RecoverQryServiceI {

    @Resource
    private RecoverEsDataQryExe recoverEsDataQryExe;

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    @Override
    public RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {
        // 1. 签署中 状态的交付单查询，交付单应符合的条件：
        //      serve.serverStatus:2 已发车，
        //      deliver.deliverStatus：3 收车中，
        //      deliver.isCheck：1 已验车，
        //      deliver.recover_contract_status：1 电子合同签署中
        //      deliver.is_insurance 0 是否退保未操作
        //      deliver.is_deduction 0 未进行处理违章操作
        // 2. 排序规则：“最近一次数据更新时间”倒序
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("serveStatus").gte(ServeEnum.DELIVER.getCode()))
                .must(QueryBuilders.rangeQuery("deliverStatus").gte(DeliverEnum.IS_RECOVER.getCode()))
                .must(QueryBuilders.matchQuery("isCheck", JudgeEnum.YES.getCode()))
                .must(QueryBuilders.matchQuery("recoverContractStatus", DeliverContractStatusEnum.SIGNING.getCode()))
                .must(QueryBuilders.matchQuery("isInsurance", JudgeEnum.NO.getCode()))
                .must(QueryBuilders.matchQuery("isDeduction", JudgeEnum.NO.getCode()));
        // es 中的 updateTime 指的是 deliver 的 updateTime
        FieldSortBuilder updateTimeSortBuilder = SortBuilders.fieldSort("updateTime").unmappedType("date").order(SortOrder.DESC);
        fieldSortBuilderList.add(updateTimeSortBuilder);

        RecoverTaskListVO recoverTaskListVO = recoverEsDataQryExe.getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList, tokenInfo);
        List<RecoverVehicleVO> recoverVehicleVOList = recoverTaskListVO.getRecoverVehicleVOList();
        if(!recoverVehicleVOList.isEmpty()){
            // 查询电子交接合同信息
            List<String> deliverNos = recoverVehicleVOList.stream().map(RecoverVehicleVO::getDeliverNo).collect(Collectors.toList());
            Result<List<ElecContractDTO>> contractDTOResult = contractAggregateRootApi.getContractDTOSByDeliverNosAndDeliverType(deliverNos, DeliverTypeEnum.RECOVER.getCode());
            List<ElecContractDTO> elecContractDTOS = ResultDataUtils.getInstance(contractDTOResult).getDataOrException();
            Map<String, ElecContractDTO> contractDTOMap = elecContractDTOS.stream().collect(Collectors.toMap(ElecContractDTO::getDeliverNos, Function.identity(), (v1, v2) -> v1));
            recoverVehicleVOList.forEach(recoverVehicleVO -> {
                String deliverNo = recoverVehicleVO.getDeliverNo();
                ElecContractDTO elecContractDTO = contractDTOMap.get(JSONUtil.toJsonStr(Collections.singletonList(deliverNo)));
                if(null != elecContractDTO){
                    recoverVehicleVO.setElecContractId(elecContractDTO.getContractId());
                    recoverVehicleVO.setElecContractStatus(elecContractDTO.getStatus());
                    recoverVehicleVO.setElecContractFailureReason(elecContractDTO.getFailureReason());
                }
            });
        }
        return recoverTaskListVO;
    }

}
