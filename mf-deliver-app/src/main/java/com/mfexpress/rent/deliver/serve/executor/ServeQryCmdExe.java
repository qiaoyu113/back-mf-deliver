package com.mfexpress.rent.deliver.serve.executor;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.mfexpress.component.starter.utils.ElasticsearchTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.dto.data.Page;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import com.mfexpress.rent.deliver.dto.es.ServeES;
import com.mfexpress.rent.deliver.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ls
 */
@Component
public class ServeQryCmdExe {


    @Resource
    private ElasticsearchTools elasticsearchTools;

    public ServeDeliverTaskListVO getServeDeliverTaskListVO(ServeDeliverTaskQryCmd serveDeliverTaskQryCmd) {
        ServeDeliverTaskListVO serveDeliverTaskListVO = new ServeDeliverTaskListVO();
        List<ServeDeliverTaskVO> serveDeliverTaskVOList = new LinkedList<>();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(serveDeliverTaskQryCmd.getCustomerName())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("customerName", serveDeliverTaskQryCmd.getCustomerName()));
        }
        if (StringUtils.isNotBlank(serveDeliverTaskQryCmd.getCustomerPhone())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("customerPhone", serveDeliverTaskQryCmd.getCustomerPhone()));
        }
        if (serveDeliverTaskQryCmd.getTag() == 1) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverStatus").lt(DeliverEnum.DELIVER.getCode()));

        } else if (serveDeliverTaskQryCmd.getTag() == 2) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.DELIVER.getCode()));
        }
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        FieldSortBuilder timeSortBuilder = SortBuilders.fieldSort("extractVehicleTime").order(SortOrder.ASC);
        fieldSortBuilderList.add(timeSortBuilder);
        //查询所有服务单
        ServeListVO serveListVO = getEsData(boolQueryBuilder, 1, 0, fieldSortBuilderList);
        List<ServeVO> serveVOList = serveListVO.getServeVOList();
        //手动分页
        if (serveVOList != null) {
            Map<Integer, List<ServeVO>> aggMap = serveVOList.stream().collect(Collectors.groupingBy(ServeVO::getOrderId));
            for (Integer orderId : aggMap.keySet()) {
                ServeDeliverTaskVO serveDeliverTaskVO = new ServeDeliverTaskVO();
                serveDeliverTaskVO.setOrderId(serveListVO.getOrderId());
                serveDeliverTaskVO.setCarModelVOList(serveListVO.getCarModelVOList());
                serveDeliverTaskVO.setCustomerName(serveListVO.getCustomerName());
                serveDeliverTaskVO.setExtractVehicleTime(serveListVO.getExtractVehicleTime());
                serveDeliverTaskVO.setStayDeliverNum(aggMap.get(orderId).size());
                serveDeliverTaskVOList.add(serveDeliverTaskVO);
            }
            //总条数
            int total = serveDeliverTaskVOList.size();
            BigDecimal bigDecimalTotal = new BigDecimal(total);
            BigDecimal bigDecimalLimit = new BigDecimal(serveDeliverTaskQryCmd.getLimit());
            BigDecimal pages = bigDecimalTotal.divide(bigDecimalLimit, BigDecimal.ROUND_UP);
            Page page = Page.builder().nowPage(serveDeliverTaskQryCmd.getPage()).pages(pages.intValue()).build();
            int start = (serveDeliverTaskQryCmd.getPage() - 1) * serveDeliverTaskQryCmd.getLimit();
            if (start + serveDeliverTaskQryCmd.getLimit() > serveDeliverTaskVOList.size()) {
                serveDeliverTaskListVO.setServeDeliverTaskVOList(serveDeliverTaskVOList.subList(start, serveDeliverTaskVOList.size()));
            } else {
                serveDeliverTaskListVO.setServeDeliverTaskVOList(serveDeliverTaskVOList.subList(start, start + serveDeliverTaskQryCmd.getLimit()));
            }
            serveDeliverTaskListVO.setPage(page);
            return serveDeliverTaskListVO;
        }
        return serveDeliverTaskListVO;

    }


    public ServeFastPreselectedListVO getServeFastPreselectedVO(ServeQryListCmd serveQryListCmd) {
        ServeFastPreselectedListVO serveFastPreselectedListVO = new ServeFastPreselectedListVO();
        List<ServeFastPreselectedVO> serveFastPreselectedVOList = new LinkedList<>();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("orderId", serveQryListCmd.getOrderId()));
        ServeListVO serveListVO = getEsData(boolQueryBuilder, serveQryListCmd.getPage(), serveQryListCmd.getLimit(), null);
        List<ServeVO> serveVOList = serveListVO.getServeVOList();

        if (serveVOList != null) {

            Map<Integer, Map<Integer, List<ServeVO>>> aggMap = serveVOList.stream().collect(Collectors.groupingBy(ServeVO::getBrandId, Collectors.groupingBy(ServeVO::getCarModelId)));
            for (Integer brandId : aggMap.keySet()) {
                //车型map
                Map<Integer, List<ServeVO>> carModelMap = aggMap.get(brandId);
                for (Integer carModeId : carModelMap.keySet()) {
                    ServeFastPreselectedVO serveFastPreselectedVO = new ServeFastPreselectedVO();
                    serveFastPreselectedVO.setCarModelId(carModeId);
                    serveFastPreselectedVO.setBrandId(brandId);
                    serveFastPreselectedVO.setServeVOList(carModelMap.get(carModeId));
                    serveFastPreselectedVO.setNum(carModelMap.get(carModeId).size());
                    serveFastPreselectedVO.setBrandModelDisplay(carModelMap.get(carModeId).get(0).getBrandModelDisplay());
                    serveFastPreselectedVOList.add(serveFastPreselectedVO);
                }
            }
            serveFastPreselectedListVO.setServeFastPreselectedVOList(serveFastPreselectedVOList);
            serveFastPreselectedListVO.setPage(serveListVO.getPage());
        }
        return serveFastPreselectedListVO;
    }

    public ServeListVO getServeListVoByOrderNoAll(ServeQryListCmd serveQryListCmd) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("orderId", serveQryListCmd.getOrderId()))
                .must(QueryBuilders.rangeQuery("deliverStatus").lte(DeliverEnum.DELIVER.getCode()));
        FieldSortBuilder sortSortBuilders = SortBuilders.fieldSort("sort").order(SortOrder.ASC);
        FieldSortBuilder updateTimeSortBuilders = SortBuilders.fieldSort("updateTime").order(SortOrder.DESC);
        List<FieldSortBuilder> fieldSortBuilders = Arrays.asList(sortSortBuilders, updateTimeSortBuilders);

        return getEsData(boolQueryBuilder, serveQryListCmd.getPage(), serveQryListCmd.getLimit(), fieldSortBuilders);


    }


    public ServePreselectedListVO getServeListVoPreselected(ServeQryListCmd serveQryListCmd) {
        //待预选数据   暂时手动处理聚合车型
        ServePreselectedListVO servePreselectedListVO = new ServePreselectedListVO();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("orderId", serveQryListCmd.getOrderId()))
                .must(QueryBuilders.matchQuery("isPreselected", JudgeEnum.NO.getCode()));
        FieldSortBuilder updateTimeSortBuilders = SortBuilders.fieldSort("updateTime").order(SortOrder.DESC);
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        fieldSortBuilderList.add(updateTimeSortBuilders);
        ServeListVO serveListVO = getEsData(boolQueryBuilder, serveQryListCmd.getPage(), serveQryListCmd.getLimit(), fieldSortBuilderList);

        List<ServeVO> serveVOList = serveListVO.getServeVOList();
        //车型聚合数据
        if (serveVOList != null) {
            List<ServePreselectedVO> servePreselectedVoList = new LinkedList<>();
            //根据carModelId分组
            Map<Integer, Map<Integer, List<ServeVO>>> aggMap = serveVOList.stream().collect(Collectors.groupingBy(ServeVO::getBrandId, Collectors.groupingBy(ServeVO::getCarModelId)));
            for (Integer brandId : aggMap.keySet()) {
                Map<Integer, List<ServeVO>> carModelMap = aggMap.get(brandId);
                for (Integer carModeId : carModelMap.keySet()) {
                    ServePreselectedVO servePreselectedVO = new ServePreselectedVO();
                    servePreselectedVO.setCarModelId(carModeId);
                    servePreselectedVO.setBrandId(brandId);
                    servePreselectedVO.setNum(carModelMap.get(carModeId).size());
                    servePreselectedVO.setServeVOList(carModelMap.get(carModeId));
                    servePreselectedVO.setBrandModelDisplay(carModelMap.get(carModeId).get(0).getBrandModelDisplay());
                    servePreselectedVoList.add(servePreselectedVO);
                }
            }
            servePreselectedListVO.setServePreselectedVOList(servePreselectedVoList);
            servePreselectedListVO.setOrderId(serveListVO.getOrderId());
            servePreselectedListVO.setContractNo(serveListVO.getContractNo());
            servePreselectedListVO.setOrderCarModelVOList(serveListVO.getCarModelVOList());
            servePreselectedListVO.setExtractVehicleTime(serveListVO.getExtractVehicleTime());
            servePreselectedListVO.setCustomerName(serveListVO.getCustomerName());
            servePreselectedListVO.setPage(serveListVO.getPage());
        }

        return servePreselectedListVO;


    }

    public ServeListVO getServeListVoCheck(ServeQryListCmd serveQryListCmd) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.must(QueryBuilders.matchQuery("orderId", serveQryListCmd.getOrderId()))
                .must(QueryBuilders.matchQuery("isPreselected", JudgeEnum.YES.getCode()))
                .must(QueryBuilders.matchQuery("isCheck", JudgeEnum.NO.getCode()))
                .must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.IS_DELIVER.getCode()));
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        FieldSortBuilder updateTimeSortBuilders = SortBuilders.fieldSort("updateTime").order(SortOrder.DESC);
        fieldSortBuilderList.add(updateTimeSortBuilders);
        return getEsData(boolQueryBuilder, serveQryListCmd.getPage(), serveQryListCmd.getLimit(), fieldSortBuilderList);
    }

    public ServeListVO getServeListVoInsure(ServeQryListCmd serveQryListCmd) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("orderId", serveQryListCmd.getOrderId()))
                .must(QueryBuilders.matchQuery("isPreselected", ServeEnum.PRESELECTED.getCode()))
                .must(QueryBuilders.matchQuery("isInsurance", JudgeEnum.NO.getCode()))
                .must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.IS_DELIVER.getCode()));
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        FieldSortBuilder updateTimeSortBuilders = SortBuilders.fieldSort("updateTime").order(SortOrder.DESC);
        fieldSortBuilderList.add(updateTimeSortBuilders);
        return getEsData(boolQueryBuilder, serveQryListCmd.getPage(), serveQryListCmd.getLimit(), fieldSortBuilderList);
    }

    public ServeListVO getServeListVoDeliver(ServeQryListCmd serveQryListCmd) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("orderId", serveQryListCmd.getOrderId()))
                .must(QueryBuilders.matchQuery("isPreselected", JudgeEnum.YES.getCode()))
                .must(QueryBuilders.matchQuery("isCheck", JudgeEnum.YES.getCode()))
                .must(QueryBuilders.matchQuery("isInsurance", JudgeEnum.YES.getCode()))
                .must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.IS_DELIVER.getCode()));
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        FieldSortBuilder updateTimeSortBuilders = SortBuilders.fieldSort("updateTime").order(SortOrder.DESC);
        fieldSortBuilderList.add(updateTimeSortBuilders);
        return getEsData(boolQueryBuilder, serveQryListCmd.getPage(), serveQryListCmd.getLimit(), fieldSortBuilderList);

    }

    public ServeListVO getServeListVoCompleted(ServeQryListCmd serveQryListCmd) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("orderId", serveQryListCmd.getOrderId()))
                .must(QueryBuilders.matchQuery("deliverStatus", DeliverEnum.DELIVER.getCode()));
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        FieldSortBuilder updateTimeSortBuilders = SortBuilders.fieldSort("updateTime").order(SortOrder.DESC);
        fieldSortBuilderList.add(updateTimeSortBuilders);

        return getEsData(boolQueryBuilder, serveQryListCmd.getPage(), serveQryListCmd.getLimit(), fieldSortBuilderList);
    }

    @SuppressWarnings("unchecked")
    private ServeListVO getEsData(QueryBuilder boolQueryBuilder, int nowPage, int limit, List<FieldSortBuilder> fieldSortBuilderList) {
        ServeListVO serveListVO = new ServeListVO();
        int start = (nowPage - 1) * limit;
        Map<String, Object> map = elasticsearchTools.searchByQuerySort(Utils.getEnvVariable(Constants.ES_DELIVER_INDEX),
                Utils.getEnvVariable(Constants.ES_DELIVER_INDEX), start, limit,
                boolQueryBuilder, fieldSortBuilderList);
        List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
        List<ServeVO> serveVoList = new LinkedList<>();
        for (Map<String, Object> serveMap : data) {
            ServeVO serveVO = new ServeVO();
            ServeES serveEs = BeanUtil.mapToBean(serveMap, ServeES.class, false, new CopyOptions());
            BeanUtil.copyProperties(serveEs, serveVO);
            serveVoList.add(serveVO);
        }
        if (data.size() > 0) {
            Map<String, Object> mapExample = data.get(0);
            ServeES serveEsExample = BeanUtil.mapToBean(mapExample, ServeES.class, false, new CopyOptions());
            serveListVO.setOrderId(serveEsExample.getOrderId());
            serveListVO.setCarModelVOList(serveEsExample.getCarModelVOList());
            serveListVO.setCustomerName(serveEsExample.getCustomerName());
            serveListVO.setContractNo(serveEsExample.getContractNo());
            serveListVO.setExtractVehicleTime(serveEsExample.getExtractVehicleTime());
        } else {
            //todo es查询订单信息
        }

        long total = (long) map.get("total");
        BigDecimal bigDecimalTotal = new BigDecimal(total);
        BigDecimal bigDecimalLimit = new BigDecimal(limit);
        BigDecimal pages = bigDecimalTotal.divide(bigDecimalLimit, BigDecimal.ROUND_UP);
        Page page = Page.builder().nowPage(nowPage).pages(pages.intValue()).total((int) total).build();
        serveListVO.setPage(page);
        serveListVO.setServeVOList(serveVoList);
        return serveListVO;

    }


}
