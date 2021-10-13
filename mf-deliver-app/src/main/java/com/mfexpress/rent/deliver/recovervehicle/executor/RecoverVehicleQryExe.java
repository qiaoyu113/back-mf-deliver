package com.mfexpress.rent.deliver.recovervehicle.executor;


import com.mfexpress.common.domain.api.OfficeAggregateRootApi;
import com.mfexpress.common.domain.dto.SysOfficeDto;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.ElasticsearchTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverApplyQryCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverApplyVO;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RecoverVehicleQryExe {

    @Resource
    private ElasticsearchTools elasticsearchTools;
    @Resource
    private OfficeAggregateRootApi officeAggregateRootApi;

    public List<RecoverApplyVO> execute(RecoverApplyQryCmd recoverApplyQryCmd, TokenInfo tokenInfo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        Result<List<SysOfficeDto>> sysOfficeResult = officeAggregateRootApi.getOfficeCityListByRegionId(tokenInfo.getOfficeId());
        if (sysOfficeResult.getCode() == 0 && sysOfficeResult.getData() != null) {
            Object[] orgIdList = sysOfficeResult.getData().stream().map(SysOfficeDto::getId).toArray();
            boolQueryBuilder.must(QueryBuilders.termsQuery("orgId", orgIdList));
        }
        //boolQueryBuilder.mustNot(QueryBuilders.matchQuery("serveStatus"))
        boolQueryBuilder.must(QueryBuilders.matchQuery("customerId", recoverApplyQryCmd.getCustomerId()))
                .must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.DELIVER.getCode()));

        if (StringUtils.isNotBlank(recoverApplyQryCmd.getContractNo())) {
            boolQueryBuilder.must(QueryBuilders.termQuery("contractNo.keyword", recoverApplyQryCmd.getContractNo()));
        }
        if (recoverApplyQryCmd.getCarModelId() != null && recoverApplyQryCmd.getCarModelId() != 0) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("carModelId", recoverApplyQryCmd.getCarModelId()));
        }
        if (recoverApplyQryCmd.getBrandId() != null && recoverApplyQryCmd.getBrandId() != 0) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("brandId", recoverApplyQryCmd.getBrandId()));

        }
        if (recoverApplyQryCmd.getStartDeliverTime() != null && recoverApplyQryCmd.getEndDeliverTime() != null) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverVehicleTime").gte(recoverApplyQryCmd.getStartDeliverTime().getTime()))
                    .must(QueryBuilders.rangeQuery("deliverVehicleTime").lte(recoverApplyQryCmd.getEndDeliverTime().getTime()));

        }
        Map<String, Object> map = elasticsearchTools.searchByQuery(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), 0, 0, boolQueryBuilder);
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) map.get("data");
        if (dataList != null) {
            return dataList.stream().map(data -> {
                RecoverApplyVO recoverApplyVO = new RecoverApplyVO();
                recoverApplyVO.setServeNo(String.valueOf(data.get("serveNo")));
                recoverApplyVO.setCarNum(String.valueOf(data.get("carNum")));
                recoverApplyVO.setDeliverNo(String.valueOf(data.get("deliverNo")));
                recoverApplyVO.setCarId(Integer.valueOf(String.valueOf(data.get("carId"))));
                return recoverApplyVO;
            }).collect(Collectors.toList());
        }
        return new ArrayList<>();

    }


}
