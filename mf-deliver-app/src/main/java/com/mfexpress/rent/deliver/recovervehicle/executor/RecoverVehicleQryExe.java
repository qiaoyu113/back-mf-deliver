package com.mfexpress.rent.deliver.recovervehicle.executor;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.mfexpress.component.starter.utils.ElasticsearchTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.dto.data.Page;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.*;
import com.mfexpress.rent.deliver.dto.es.ServeES;
import com.mfexpress.rent.deliver.utils.Utils;
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
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RecoverVehicleQryExe {

    @Resource
    private ElasticsearchTools elasticsearchTools;


    public List<RecoverApplyVO> getRecoverVehicleListVO(RecoverApplyQryCmd recoverApplyQryCmd) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("orderId", recoverApplyQryCmd.getOrderId()))
                .must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.DELIVER.getCode()));

        if (StringUtils.isNotBlank(recoverApplyQryCmd.getContractNo())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("contractNo", recoverApplyQryCmd.getContractNo()));
        }
        if (recoverApplyQryCmd.getCarModelId() != null && recoverApplyQryCmd.getCarModelId() != 0) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("carModelId", recoverApplyQryCmd.getCarModelId()));
        }
        if (recoverApplyQryCmd.getBrandId() != null && recoverApplyQryCmd.getBrandId() != 0) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("brandId", recoverApplyQryCmd.getBrandId()));

        }
        if (recoverApplyQryCmd.getStartDeliverTime() != null && recoverApplyQryCmd.getEndDeliverTime() != null) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverVehicleTime").gte(recoverApplyQryCmd.getStartDeliverTime()))
                    .must(QueryBuilders.rangeQuery("deliverVehicleTime").lte(recoverApplyQryCmd.getEndDeliverTime()));

        }
        Map<String, Object> map = elasticsearchTools.searchByQuery(Utils.getEnvVariable(Constants.ES_DELIVER_INDEX), Utils.getEnvVariable(Constants.ES_DELIVER_INDEX), 0, 0, boolQueryBuilder);
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

    public RecoverTaskListVO getRecoverApplyListAll(RecoverQryListCmd recoverQryListCmd) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverStatus").gte(DeliverEnum.IS_RECOVER.getCode()));
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        if (!recoverQryListCmd.getSortTag().equals(0) && recoverQryListCmd.getSortTag().equals(1)) {
            FieldSortBuilder checkSortBuilder = SortBuilders.fieldSort("isCheck").order(SortOrder.ASC);
            FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("expectRecoverTime").order(SortOrder.ASC);
            fieldSortBuilderList = Arrays.asList(checkSortBuilder, timeSortBuilder);
        } else if (!recoverQryListCmd.getSortTag().equals(0) && recoverQryListCmd.getSortTag().equals(2)) {
            FieldSortBuilder sortSortBuilder = SortBuilders.fieldSort("sort").order(SortOrder.ASC);
            FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("expectRecoverTime").order(SortOrder.ASC);
            fieldSortBuilderList = Arrays.asList(sortSortBuilder, timeSortBuilder);
        }

        return getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList);
    }

    public RecoverTaskListVO getStayRecoverApplyList(RecoverQryListCmd recoverQryListCmd) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.IS_RECOVER.getCode()))
                .must(QueryBuilders.matchQuery("isCheck", JudgeEnum.NO.getCode()));
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("expectRecoverTime").order(SortOrder.ASC);
        fieldSortBuilderList.add(timeSortBuilder);
        return getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList);
    }

    public RecoverTaskListVO getCompletedRecoverApplyList(RecoverQryListCmd recoverQryListCmd) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverStatus").gte(DeliverEnum.IS_RECOVER.getCode()))
                .must(QueryBuilders.matchQuery("isCheck", JudgeEnum.YES.getCode()));
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("expectRecoverTime").order(SortOrder.ASC);
        fieldSortBuilderList.add(timeSortBuilder);
        return getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList);

    }

    public RecoverTaskListVO getRecoverTaskListVoInsure(RecoverQryListCmd recoverQryListCmd) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.IS_RECOVER.getCode()))
                .must(QueryBuilders.matchQuery("isCheck", JudgeEnum.YES.getCode()))
                .must(QueryBuilders.matchQuery("isInsurance", JudgeEnum.NO.getCode()));

        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("recoverVehicleTime").order(SortOrder.DESC);
        fieldSortBuilderList.add(timeSortBuilder);
        return getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList);

    }

    public RecoverTaskListVO getRecoverTaskListVoDeduction(RecoverQryListCmd recoverQryListCmd) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.IS_RECOVER.getCode()))
                .must(QueryBuilders.matchQuery("isCheck", JudgeEnum.YES.getCode()))
                .must(QueryBuilders.matchQuery("isDeduction", JudgeEnum.NO.getCode()));

        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("recoverVehicleTime").order(SortOrder.DESC);
        fieldSortBuilderList.add(timeSortBuilder);
        return getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList);
    }

    public RecoverTaskListVO getRecoverTaskListVoCompleted(RecoverQryListCmd recoverQryListCmd) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("serveStatus", ServeEnum.COMPLETED.getCode()));
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("recoverVehicleTime").order(SortOrder.DESC);
        fieldSortBuilderList.add(timeSortBuilder);

        return getEsData(recoverQryListCmd, boolQueryBuilder, fieldSortBuilderList);
    }

    @SuppressWarnings("unchecked")
    private RecoverTaskListVO getEsData(RecoverQryListCmd recoverQryListCmd, BoolQueryBuilder boolQueryBuilder
            , List<FieldSortBuilder> fieldSortBuilderList) {
        RecoverTaskListVO recoverTaskListVO = new RecoverTaskListVO();

        if (StringUtils.isNotBlank(recoverQryListCmd.getCustomerName())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("customerName", recoverQryListCmd.getCustomerName()));
        }
        if (StringUtils.isNotBlank(recoverQryListCmd.getCustomerPhone())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("customerPhone", recoverQryListCmd.getCustomerPhone()));
        }
        if (recoverQryListCmd.getCarModelId() != null && recoverQryListCmd.getCarModelId() != 0) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("carModelId", recoverQryListCmd.getCarModelId()));
        }
        if (recoverQryListCmd.getExpectRecoverTime() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("expectRecoverTime", recoverQryListCmd.getExpectRecoverTime()));
        }
        if (recoverQryListCmd.getStartDeliverTime() != null && recoverQryListCmd.getEndDeliverTime() != null) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverVehicleTime").gte(recoverQryListCmd.getStartDeliverTime()))
                    .must(QueryBuilders.rangeQuery("deliverVehicleTime").lte(recoverQryListCmd.getEndDeliverTime()));
        }
        int start = (recoverQryListCmd.getPage() - 1) * recoverQryListCmd.getLimit();
        Map<String, Object> map = elasticsearchTools.searchByQuerySort(Utils.getEnvVariable(Constants.ES_DELIVER_INDEX),
                Utils.getEnvVariable(Constants.ES_DELIVER_INDEX), start, recoverQryListCmd.getLimit(), boolQueryBuilder, fieldSortBuilderList
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
        Page page = Page.builder().nowPage(recoverQryListCmd.getPage()).pages(pages.intValue()).build();
        recoverTaskListVO.setRecoverVehicleVOList(recoverVehicleVOList);
        recoverTaskListVO.setPage(page);

        return recoverTaskListVO;

    }

}
