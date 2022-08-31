package com.mfexpress.rent.deliver.recovervehicle.executor;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.mfexpress.common.domain.api.OfficeAggregateRootApi;
import com.mfexpress.common.domain.dto.SysOfficeDto;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.ElasticsearchTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.dto.data.Page;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverQryListCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverTaskListVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleVO;
import com.mfexpress.rent.deliver.dto.es.ServeES;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.PolicyStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInsuranceDTO;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RecoverEsDataQryExe {
    @Resource
    private ElasticsearchTools elasticsearchTools;
    @Resource
    private OfficeAggregateRootApi officeAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    public RecoverTaskListVO getEsData(RecoverQryListCmd recoverQryListCmd, BoolQueryBuilder boolQueryBuilder
            , List<FieldSortBuilder> fieldSortBuilderList, TokenInfo tokenInfo, String index, String type) {

        RecoverTaskListVO recoverTaskListVO = new RecoverTaskListVO();

        List<FieldSortBuilder> sortBuilderList = new LinkedList<>();
        FieldSortBuilder scoreSortBuilder = SortBuilders.fieldSort("_score").order(SortOrder.DESC);
        sortBuilderList.add(scoreSortBuilder);
        sortBuilderList.addAll(fieldSortBuilderList);

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

        if (StringUtils.isNotEmpty(recoverQryListCmd.getPlateNumber())) {
            boolQueryBuilder.must(QueryBuilders.termQuery("carNum.keyword", recoverQryListCmd.getPlateNumber()));
        }

        int start = (recoverQryListCmd.getPage() - 1) * recoverQryListCmd.getLimit();

        sortBuilderList.addAll(fieldSortBuilderList);

        Map<String, Object> map = elasticsearchTools.searchByQuerySort(DeliverUtils.getEnvVariable(index),
                type, start, recoverQryListCmd.getLimit(), boolQueryBuilder, sortBuilderList
        );
        List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
        long total = (long) map.get("total");
        LinkedList<RecoverVehicleVO> recoverVehicleVOList = new LinkedList<>();
        List<Integer> vehicleIdList = new ArrayList<>();
        for (Map<String, Object> dataMap : data) {
            RecoverVehicleVO recoverVehicleVO = new RecoverVehicleVO();
            ServeES serveEs = BeanUtil.mapToBean(dataMap, ServeES.class, false, new CopyOptions());
            BeanUtils.copyProperties(serveEs, recoverVehicleVO);
            recoverVehicleVOList.add(recoverVehicleVO);
            vehicleIdList.add(recoverVehicleVO.getCarId());
        }

        if (!vehicleIdList.isEmpty()) {
            Result<List<VehicleInsuranceDTO>> vehicleInsuranceDTOSResult = vehicleAggregateRootApi.getVehicleInsuranceByVehicleIds(vehicleIdList);
            List<VehicleInsuranceDTO> vehicleInsuranceDTOS = ResultDataUtils.getInstance(vehicleInsuranceDTOSResult).getDataOrException();
            Map<Integer, VehicleInsuranceDTO> vehicleInsuranceDTOMap = vehicleInsuranceDTOS.stream().collect(Collectors.toMap(VehicleInsuranceDTO::getVehicleId, Function.identity(), (v1, v2) -> v1));
            for (RecoverVehicleVO recoverVehicleVO : recoverVehicleVOList) {
                VehicleInsuranceDTO vehicleInsuranceDTO = vehicleInsuranceDTOMap.get(recoverVehicleVO.getCarId());
                if (null != vehicleInsuranceDTO) {
                    Integer compulsoryInsuranceStatus = vehicleInsuranceDTO.getCompulsoryInsuranceStatus();
                    recoverVehicleVO.setVehicleCompulsoryInsuranceStatus(compulsoryInsuranceStatus);
                    if (null != compulsoryInsuranceStatus) {
                        recoverVehicleVO.setVehicleCompulsoryInsuranceStatusDisplay(getInsuranceStatusName(vehicleInsuranceDTO.getCompulsoryInsuranceStatus(), vehicleInsuranceDTO.getCompulsoryInsuranceEndDate()));
                    }
                    recoverVehicleVO.setVehicleCompulsoryInsuranceEndDate(vehicleInsuranceDTO.getCompulsoryInsuranceEndDate());

                    Integer commercialInsuranceStatus = vehicleInsuranceDTO.getCommercialInsuranceStatus();
                    recoverVehicleVO.setVehicleCommercialInsuranceStatus(commercialInsuranceStatus);
                    if (null != commercialInsuranceStatus) {
                        recoverVehicleVO.setVehicleCommercialInsuranceStatusDisplay(getInsuranceStatusName(vehicleInsuranceDTO.getCommercialInsuranceStatus(), vehicleInsuranceDTO.getCommercialInsuranceEndDate()));
                    }
                    recoverVehicleVO.setVehicleCommercialInsuranceEndDate(vehicleInsuranceDTO.getCommercialInsuranceEndDate());
                }
            }
        }

        BigDecimal bigDecimalTotal = new BigDecimal(total);
        BigDecimal bigDecimalLimit = new BigDecimal(recoverQryListCmd.getLimit());

        BigDecimal pages = bigDecimalTotal.divide(bigDecimalLimit, BigDecimal.ROUND_UP);
        Page page = Page.builder().nowPage(recoverQryListCmd.getPage()).pages(pages.intValue()).total((int) total).build();
        recoverTaskListVO.setRecoverVehicleVOList(recoverVehicleVOList);
        recoverTaskListVO.setPage(page);

        return recoverTaskListVO;

    }

    private String getInsuranceStatusName(Integer status, Date endDate) {
        if (Objects.isNull(status)) {
            return "";
        }
        if (status == PolicyStatusEnum.EXPIRED.getCode()) {
            return PolicyStatusEnum.EXPIRED.getName();
        } else {
            StringBuilder statusName = new StringBuilder();
            if (new Date().after(DateUtil.offset(endDate, DateField.DAY_OF_YEAR, -30))) {
                statusName.append("即将过期");
            } else {
                statusName.append("生效中");
            }
            return statusName.append("(").append(DateUtil.format(endDate, "yyyy-MM-dd")).append(")").toString();
        }
    }
}
