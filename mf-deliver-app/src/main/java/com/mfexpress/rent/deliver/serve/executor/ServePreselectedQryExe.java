package com.mfexpress.rent.deliver.serve.executor;

import com.mfexpress.rent.deliver.api.SyncServiceI;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
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
public class ServePreselectedQryExe {

    @Resource
    private ServeEsDataQryExe serveEsDataQryExe;
    @Resource
    private SyncServiceI syncServiceI;

    public ServePreselectedListVO execute(ServeQryListCmd serveQryListCmd) {
        //待预选数据   暂时手动处理聚合车型
        ServePreselectedListVO servePreselectedListVO = new ServePreselectedListVO();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("orderId", serveQryListCmd.getOrderId()))
                .must(QueryBuilders.matchQuery("isPreselected", JudgeEnum.NO.getCode()));
        FieldSortBuilder updateTimeSortBuilders = SortBuilders.fieldSort("updateTime").unmappedType("integer").order(SortOrder.DESC);
        List<FieldSortBuilder> fieldSortBuilderList = new LinkedList<>();
        fieldSortBuilderList.add(updateTimeSortBuilders);
        ServeListVO serveListVO = serveEsDataQryExe.execute(serveQryListCmd.getOrderId(), boolQueryBuilder, serveQryListCmd.getPage(), serveQryListCmd.getLimit(), fieldSortBuilderList);
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
            servePreselectedListVO.setCustomerId(serveListVO.getCustomerId());
        }

        return servePreselectedListVO;
    }

}

