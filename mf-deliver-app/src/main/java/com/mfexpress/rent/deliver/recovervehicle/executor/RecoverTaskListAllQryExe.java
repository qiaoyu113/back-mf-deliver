package com.mfexpress.rent.deliver.recovervehicle.executor;

import cn.hutool.json.JSONUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RecoverTaskListAllQryExe implements RecoverQryServiceI {

    @Resource
    private RecoverEsDataQryExe recoverEsDataQryExe;

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    @Override
    public RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverStatus").gte(DeliverEnum.IS_RECOVER.getCode()));
        FieldSortBuilder sortSortBuilder = SortBuilders.fieldSort("sort").order(SortOrder.ASC);
        // 收车任务1.4迭代
        //    1. 第一排序规则：“待验车”＞“待收车”＞“签署中”＞“待退保”＞“待处理违章”＞“已完成”
        //    2. 第二排序规则：“最近一次数据更新时间”倒序
        //FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("expectRecoverTime").unmappedType("integer").order(SortOrder.ASC);
        FieldSortBuilder updateTimeSortBuilder = SortBuilders.fieldSort("updateTime").unmappedType("integer").order(SortOrder.DESC);

        List<FieldSortBuilder> fieldSortBuilderList = Arrays.asList(sortSortBuilder, updateTimeSortBuilder);
        RecoverTaskListVO recoverTaskListVO = recoverEsDataQryExe.getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList, tokenInfo);

        List<String> deliverNos = new ArrayList<>();
        List<RecoverVehicleVO> recoverVehicleVOList = recoverTaskListVO.getRecoverVehicleVOList();
        recoverVehicleVOList.forEach(recoverVehicleVO -> {
            if(ServeEnum.RECOVER.getCode() <= recoverVehicleVO.getServeStatus() && DeliverEnum.RECOVER.getCode().equals(recoverVehicleVO.getDeliverStatus())){
                if (JudgeEnum.YES.getCode().equals(recoverVehicleVO.getRecoverAbnormalFlag())) {
                    recoverVehicleVO.setRecoverTypeDisplay(RecoverVehicleType.ABNORMAL.getName());
                } else if(DeliverContractStatusEnum.COMPLETED.getCode() == recoverVehicleVO.getRecoverContractStatus()){
                    recoverVehicleVO.setRecoverTypeDisplay(RecoverVehicleType.NORMAL.getName());
                }
                if(JudgeEnum.NO.getCode().equals(recoverVehicleVO.getRecoverAbnormalFlag()) && DeliverContractStatusEnum.NOSIGN.getCode() == recoverVehicleVO.getRecoverContractStatus()){
                    recoverVehicleVO.setRecoverTypeDisplay(RecoverVehicleType.NORMAL.getName());
                }
            }
            if (null != recoverVehicleVO.getRecoverContractStatus() && (DeliverContractStatusEnum.GENERATING.getCode() == recoverVehicleVO.getRecoverContractStatus() || DeliverContractStatusEnum.SIGNING.getCode() == recoverVehicleVO.getRecoverContractStatus())) {
                deliverNos.add(recoverVehicleVO.getDeliverNo());
            }
        });

        if(!deliverNos.isEmpty()){
            Result<List<ElecContractDTO>> contractDTOSResult = contractAggregateRootApi.getContractDTOSByDeliverNosAndDeliverType(deliverNos, DeliverTypeEnum.RECOVER.getCode());
            if(ResultErrorEnum.SUCCESSED.getCode() == contractDTOSResult.getCode() && null != contractDTOSResult.getData() && !contractDTOSResult.getData().isEmpty()){
                Map<String, ElecContractDTO> contractDTOMap = contractDTOSResult.getData().stream().collect(Collectors.toMap(ElecContractDTO::getDeliverNos, Function.identity(), (v1, v2) -> v1));
                recoverVehicleVOList.forEach(recoverVehicleVO -> {
                    String deliverNo = recoverVehicleVO.getDeliverNo();
                    ElecContractDTO elecContractDTO = contractDTOMap.get(JSONUtil.toJsonStr(Collections.singletonList(deliverNo)));
                    if(null != elecContractDTO){
                        recoverVehicleVO.setElecContractId(elecContractDTO.getContractId().toString());
                        recoverVehicleVO.setElecContractStatus(elecContractDTO.getStatus());
                        recoverVehicleVO.setElecContractFailureReason(elecContractDTO.getFailureReason());
                    }
                });
            }
        }
        return recoverTaskListVO;
    }
}
