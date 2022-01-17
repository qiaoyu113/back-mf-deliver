package com.mfexpress.rent.deliver.recovervehicle.executor;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.mfexpress.common.domain.api.OfficeAggregateRootApi;
import com.mfexpress.common.domain.dto.SysOfficeDto;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.ElasticsearchTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.dto.data.Page;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverQryListCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverTaskListVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleVO;
import com.mfexpress.rent.deliver.dto.es.ServeES;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class RecoverEsDataQryExe {
    @Resource
    private ElasticsearchTools elasticsearchTools;
    @Resource
    private OfficeAggregateRootApi officeAggregateRootApi;

    public RecoverTaskListVO getEsData(RecoverQryListCmd recoverQryListCmd, BoolQueryBuilder boolQueryBuilder
            , List<FieldSortBuilder> fieldSortBuilderList, TokenInfo tokenInfo) {
        List<FieldSortBuilder> sortBuilderList = new LinkedList<>();
        FieldSortBuilder scoreSortBuilder = SortBuilders.fieldSort("_score").order(SortOrder.DESC);
        sortBuilderList.add(scoreSortBuilder);
        sortBuilderList.addAll(fieldSortBuilderList);

        RecoverTaskListVO recoverTaskListVO = new RecoverTaskListVO();

        Result<List<SysOfficeDto>> sysOfficeResult = officeAggregateRootApi.getOfficeCityListByRegionId(tokenInfo.getOfficeId());
        if (sysOfficeResult.getCode() == 0 && sysOfficeResult.getData() != null) {
            List<SysOfficeDto> sysOfficeDtoList = sysOfficeResult.getData();
            Object[] orgIdList = sysOfficeDtoList.stream().map(SysOfficeDto::getId).toArray();
            boolQueryBuilder.must(QueryBuilders.termsQuery("orgId", orgIdList));
        }

        if (StringUtils.isNotBlank(recoverQryListCmd.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.multiMatchQuery(recoverQryListCmd.getKeyword(), "customerName", "customerPhone"));
        }
        if (recoverQryListCmd.getCarModelId() != null && recoverQryListCmd.getCarModelId() != 0) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("carModelId", recoverQryListCmd.getCarModelId()));
        }
        if (recoverQryListCmd.getExpectRecoverStartTime() != null && recoverQryListCmd.getExpectRecoverEndTime() != null) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery("expectRecoverTime").from(recoverQryListCmd.getExpectRecoverStartTime().getTime()).to(recoverQryListCmd.getExpectRecoverEndTime().getTime()));
        }
        if (recoverQryListCmd.getStartDeliverTime() != null && recoverQryListCmd.getEndDeliverTime() != null) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverVehicleTime").from(recoverQryListCmd.getStartDeliverTime().getTime()).to(recoverQryListCmd.getEndDeliverTime().getTime()));

        }
        int start = (recoverQryListCmd.getPage() - 1) * recoverQryListCmd.getLimit();
        Map<String, Object> map = elasticsearchTools.searchByQuerySort(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX),
                DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), start, recoverQryListCmd.getLimit(), boolQueryBuilder, sortBuilderList
        );
        List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
        long total = (long) map.get("total");
        LinkedList<RecoverVehicleVO> recoverVehicleVOList = new LinkedList<>();
        for (Map<String, Object> dataMap : data) {
            RecoverVehicleVO recoverVehicleVO = new RecoverVehicleVO();
            ServeES serveEs = BeanUtil.mapToBean(dataMap, ServeES.class, false, new CopyOptions());
            BeanUtils.copyProperties(serveEs, recoverVehicleVO);
            recoverVehicleVOList.add(recoverVehicleVO);
        }

        BigDecimal bigDecimalTotal = new BigDecimal(total);
        BigDecimal bigDecimalLimit = new BigDecimal(recoverQryListCmd.getLimit());

        BigDecimal pages = bigDecimalTotal.divide(bigDecimalLimit, BigDecimal.ROUND_UP);
        Page page = Page.builder().nowPage(recoverQryListCmd.getPage()).pages(pages.intValue()).total((int) total).build();
        recoverTaskListVO.setRecoverVehicleVOList(recoverVehicleVOList);
        recoverTaskListVO.setPage(page);

        return recoverTaskListVO;

    }
}
