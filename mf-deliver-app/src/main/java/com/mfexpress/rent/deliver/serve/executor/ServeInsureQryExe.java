package com.mfexpress.rent.deliver.serve.executor;

import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.order.api.app.ContractAggregateRootApi;
import com.mfexpress.order.dto.data.CommodityDTO;
import com.mfexpress.order.dto.data.InsuranceInfoDTO;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.InsuranceApplyDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeListVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryListCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeVO;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ServeInsureQryExe {

    @Resource
    private ServeEsDataQryExe serveEsDataQryExe;

    @Resource
    private ContractAggregateRootApi contractAggregateRootApi;

    /*@Resource
    private SyncServiceI syncServiceI;*/

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    public ServeListVO execute(ServeQryListCmd serveQryListCmd) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("orderId", serveQryListCmd.getOrderId()))
                .must(QueryBuilders.matchQuery("isPreselected", ServeEnum.PRESELECTED.getCode()))
                .must(QueryBuilders.matchQuery("isInsurance", JudgeEnum.NO.getCode()))
                .must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.IS_DELIVER.getCode()))
                .mustNot(QueryBuilders.matchQuery("serveStatus", ServeEnum.CANCEL.getCode()));
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        FieldSortBuilder updateTimeSortBuilders = SortBuilders.fieldSort("updateTime").unmappedType("integer").order(SortOrder.DESC);
        fieldSortBuilderList.add(updateTimeSortBuilders);
        ServeListVO serveListVO = serveEsDataQryExe.execute(serveQryListCmd.getOrderId(), boolQueryBuilder, serveQryListCmd.getPage(), serveQryListCmd.getLimit(), fieldSortBuilderList);

        // 设置保险按钮显示标志位
        List<ServeVO> serveVOList = serveListVO.getServeVOList();
        serveEsDataQryExe.supplyVehicleInsureRequirement(serveVOList);
        if (serveVOList.isEmpty()) {
            serveListVO.setBatchInsureButtonSwitch(JudgeEnum.NO.getCode());
        } else {
            Integer batchInsureButtonSwitch = 1;
            List<Integer> commodityIdList = serveVOList.stream().map(ServeVO::getContractCommodityId).collect(Collectors.toList());
            Result<List<CommodityDTO>> commodityListResult = contractAggregateRootApi.getCommodityListByIdList(commodityIdList);
            List<CommodityDTO> commodityDTOList = ResultDataUtils.getInstance(commodityListResult).getDataOrException();
            Map<Integer, CommodityDTO> commodityDTOMap = commodityDTOList.stream().collect(Collectors.toMap(CommodityDTO::getId, Function.identity(), (v1, v2) -> v1));
            for (ServeVO serveVO : serveVOList) {
                CommodityDTO commodityDTO = commodityDTOMap.get(serveVO.getContractCommodityId());
                if (null == commodityDTO) {
                    // 1：显示投保申请按钮，2：显示录入保单信息按钮，假如商品信息未查到，默认赋0
                    serveVO.setOperationButton(0);
                } else {
                    InsuranceInfoDTO insuranceInfo = commodityDTO.getInsuranceInfo();
                    if (null == insuranceInfo) {
                        serveVO.setOperationButton(0);
                    } else {
                        if (null != insuranceInfo.getThirdPartyLiabilityCoverage() || null != insuranceInfo.getInCarPersonnelLiabilityCoverage()) {
                            serveVO.setOperationButton(1);
                        } else {
                            serveVO.setOperationButton(2);
                            batchInsureButtonSwitch = 0;
                        }
                    }
                }
            }
            serveListVO.setBatchInsureButtonSwitch(batchInsureButtonSwitch);

            // 补充insureApplyFlag
            List<String> deliverNoList = serveVOList.stream().map(ServeVO::getDeliverNo).collect(Collectors.toList());
            Result<List<InsuranceApplyDTO>> insureApplyDTOListResult = deliverAggregateRootApi.getInsuranceApplyListByDeliverNoList(deliverNoList);
            List<InsuranceApplyDTO> insuranceApplyDTOList = ResultDataUtils.getInstance(insureApplyDTOListResult).getDataOrException();
            Map<String, InsuranceApplyDTO> insureApplyDTOMap = insuranceApplyDTOList.stream().collect(Collectors.toMap(InsuranceApplyDTO::getDeliverNo, Function.identity(), (v1, v2) -> v1));
            for (ServeVO serveVO : serveVOList) {
                InsuranceApplyDTO insuranceApplyDTO = insureApplyDTOMap.get(serveVO.getDeliverNo());
                if (null != insuranceApplyDTO) {
                    if (!StringUtils.isEmpty(insuranceApplyDTO.getCompulsoryApplyId()) || !StringUtils.isEmpty(insuranceApplyDTO.getCommercialApplyId())) {
                        serveVO.setInsureApplyFlag(JudgeEnum.YES.getCode());
                    }
                }
                if (null == serveVO.getInsureApplyFlag()) {
                    serveVO.setInsureApplyFlag(JudgeEnum.NO.getCode());
                }
            }
        }

        return serveListVO;
    }
}
