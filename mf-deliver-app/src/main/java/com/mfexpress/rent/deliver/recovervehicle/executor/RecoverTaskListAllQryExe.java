package com.mfexpress.rent.deliver.recovervehicle.executor;

import cn.hutool.json.JSONUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverQryListCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverTaskListVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleVO;
import com.mfexpress.rent.deliver.dto.entity.RecoverVehicle;
import com.mfexpress.rent.deliver.recovervehicle.RecoverQryServiceI;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
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

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Override
    public RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverStatus").gte(DeliverEnum.IS_RECOVER.getCode()));
        // 只查询sort为7、8、9、24、25、26的数据，详见DeliverSyncServiceImpl和ServeSyncServiceImpl类中的getSort方法
        boolQueryBuilder.must(QueryBuilders.termsQuery("sort", Arrays.asList(DeliverSortEnum.SEVEN.getSort(), DeliverSortEnum.EIGHT.getSort(), DeliverSortEnum.NINE.getSort(), DeliverSortEnum.TWENTY_FOUR.getSort(),
                DeliverSortEnum.TWENTY_FIVE.getSort(), DeliverSortEnum.TWENTY_SIX.getSort())));

        FieldSortBuilder sortBuilder = SortBuilders.fieldSort("sort").order(SortOrder.ASC);
        // 收车任务1.4迭代
        //    1. 第一排序规则：“待验车”＞“待收车”＞“签署中”＞“待退保”＞“待处理违章”＞“已完成”
        //    2. 第二排序规则：“最近一次数据更新时间”倒序
        FieldSortBuilder expectRecoverTimeBuilder = SortBuilders.fieldSort("expectRecoverTime").unmappedType("integer").order(SortOrder.DESC);
        FieldSortBuilder updateTimeSortBuilder = SortBuilders.fieldSort("updateTime").unmappedType("integer").order(SortOrder.DESC);

        List<FieldSortBuilder> fieldSortBuilderList = Arrays.asList(sortBuilder, expectRecoverTimeBuilder, updateTimeSortBuilder);
        RecoverTaskListVO esData = recoverEsDataQryExe.getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList, tokenInfo, "recover_list_all", null);

        List<String> deliverNos = new ArrayList<>();
        List<RecoverVehicleVO> recoverVehicleVOList = esData.getRecoverVehicleVOList();
        if (!recoverVehicleVOList.isEmpty()) {
            List<String> needSupplementFeeServeNoList = new ArrayList<>();
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
        }
        return esData;
    }
}
