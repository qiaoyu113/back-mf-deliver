package com.mfexpress.rent.deliver.serve.executor;

import com.mfexpress.rent.deliver.dto.data.serve.*;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class ServeFastPreselectedQryExe {
    @Resource
    private ServeEsDataQryExe serveEsDataQryExe;

    public ServeFastPreselectedListVO execute(ServeQryListCmd serveQryListCmd) {
        ServeFastPreselectedListVO serveFastPreselectedListVO = new ServeFastPreselectedListVO();
        List<ServeFastPreselectedVO> serveFastPreselectedVOList = new LinkedList<>();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        LinkedList<FieldSortBuilder> fieldSortBuilders = new LinkedList<>();
        boolQueryBuilder.must(QueryBuilders.matchQuery("orderId", serveQryListCmd.getOrderId()));
        FieldSortBuilder updateTimeSort = SortBuilders.fieldSort("updateTime").unmappedType("integer").order(SortOrder.DESC);
        fieldSortBuilders.add(updateTimeSort);
        ServeListVO serveListVO = serveEsDataQryExe.execute(serveQryListCmd.getOrderId(), boolQueryBuilder, serveQryListCmd.getPage(), serveQryListCmd.getLimit(), fieldSortBuilders);
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
}
