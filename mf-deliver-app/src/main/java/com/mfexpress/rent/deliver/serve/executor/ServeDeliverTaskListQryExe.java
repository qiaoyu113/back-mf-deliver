package com.mfexpress.rent.deliver.serve.executor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.mfexpress.common.domain.api.OfficeAggregateRootApi;
import com.mfexpress.common.domain.dto.SysOfficeDto;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.ElasticsearchTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.dto.data.Page;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDeliverTaskListVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDeliverTaskQryCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDeliverTaskVO;
import com.mfexpress.rent.deliver.dto.es.ServeES;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ServeDeliverTaskListQryExe {

    @Resource
    private ElasticsearchTools elasticsearchTools;
    @Resource
    private OfficeAggregateRootApi officeAggregateRootApi;

    public ServeDeliverTaskListVO execute(ServeDeliverTaskQryCmd serveDeliverTaskQryCmd, TokenInfo tokenInfo) {

        ServeDeliverTaskListVO serveDeliverTaskListVO = new ServeDeliverTaskListVO();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        List<ServeDeliverTaskVO> serveDeliverTaskVOList = new LinkedList<>();
        try {

            Result<List<SysOfficeDto>> sysOfficeResult = officeAggregateRootApi.getOfficeCityListByRegionId(tokenInfo.getOfficeId());
            if (sysOfficeResult.getCode() == 0 && sysOfficeResult.getData() != null) {
                List<SysOfficeDto> sysOfficeDtoList = sysOfficeResult.getData();
                Object[] orgIdList = sysOfficeDtoList.stream().map(SysOfficeDto::getId).toArray();
                boolQueryBuilder.must(QueryBuilders.termsQuery("orgId", orgIdList));
            }
        } catch (Exception e) {
            boolQueryBuilder.must(QueryBuilders.termQuery("orgId", tokenInfo.getOfficeId()));
        }

        if (StringUtils.isNotBlank(serveDeliverTaskQryCmd.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.multiMatchQuery(serveDeliverTaskQryCmd.getKeyword(), "customerName", "customerPhone"));
        }
        if (serveDeliverTaskQryCmd.getTag() == 1) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverStatus").lt(DeliverEnum.DELIVER.getCode()));

        } else if (serveDeliverTaskQryCmd.getTag() == 2) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery("deliverStatus").gte(DeliverEnum.DELIVER.getCode()));
        }

        //查询所有服务单
        Map<String, Object> map = elasticsearchTools.searchByQuery(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX),
                DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), 0, 0,
                boolQueryBuilder);
        List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
        List<ServeES> serveEsList = new LinkedList<>();
        for (Map<String, Object> serveMap : data) {
            ServeES serveEs = BeanUtil.mapToBean(serveMap, ServeES.class, false, new CopyOptions());

            serveEsList.add(serveEs);
        }
        //手动分页

        Map<String, List<ServeES>> aggMap = serveEsList.stream().collect(Collectors.groupingBy(ServeES::getOrderId));
        for (String orderId : aggMap.keySet()) {
            ServeDeliverTaskVO serveDeliverTaskVO = new ServeDeliverTaskVO();
            serveDeliverTaskVO.setOrderId(orderId);
            serveDeliverTaskVO.setCarModelVOList(aggMap.get(orderId).get(0).getCarModelVOList());
            serveDeliverTaskVO.setCustomerName(aggMap.get(orderId).get(0).getCustomerName());
            serveDeliverTaskVO.setExtractVehicleTime(aggMap.get(orderId).get(0).getExtractVehicleTime());
            serveDeliverTaskVO.setStayDeliverNum(aggMap.get(orderId).size());
            serveDeliverTaskVO.setContractNo(aggMap.get(orderId).get(0).getContractNo());
            serveDeliverTaskVOList.add(serveDeliverTaskVO);
        }
        // serveDeliverTaskVOList = serveDeliverTaskVOList.stream().sorted(Comparator.comparing(ServeDeliverTaskVO::getExtractVehicleTime)).collect(Collectors.toList());
        //总条数
        int total = serveDeliverTaskVOList.size();
        BigDecimal bigDecimalTotal = new BigDecimal(total);
        BigDecimal bigDecimalLimit = new BigDecimal(serveDeliverTaskQryCmd.getLimit());
        BigDecimal pages = bigDecimalTotal.divide(bigDecimalLimit, BigDecimal.ROUND_UP);
        Page page = Page.builder().nowPage(serveDeliverTaskQryCmd.getPage()).pages(pages.intValue()).total(total).build();
        int start = (serveDeliverTaskQryCmd.getPage() - 1) * serveDeliverTaskQryCmd.getLimit();
        if (start > total) {
            serveDeliverTaskListVO.setServeDeliverTaskVOList(new ArrayList<>());
        }
        if (start + serveDeliverTaskQryCmd.getLimit() > total) {
            serveDeliverTaskListVO.setServeDeliverTaskVOList(serveDeliverTaskVOList.subList(start, serveDeliverTaskVOList.size()));
        } else {
            serveDeliverTaskListVO.setServeDeliverTaskVOList(serveDeliverTaskVOList.subList(start, start + serveDeliverTaskQryCmd.getLimit()));
        }
        serveDeliverTaskListVO.setPage(page);

        return serveDeliverTaskListVO;
    }
}
