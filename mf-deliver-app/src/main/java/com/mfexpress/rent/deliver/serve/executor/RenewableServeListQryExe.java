package com.mfexpress.rent.deliver.serve.executor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.mfexpress.common.domain.api.OfficeAggregateRootApi;
import com.mfexpress.common.domain.dto.SysOfficeDto;
import com.mfexpress.common.domain.enums.OfficeCodeMsgEnum;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.response.ResultStatusEnum;
import com.mfexpress.component.starter.utils.ElasticsearchTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.dto.data.serve.RenewableServeQry;
import com.mfexpress.rent.deliver.dto.data.serve.ServeToRenewalVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeVO;
import com.mfexpress.rent.deliver.dto.es.ServeES;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.utils.Utils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RenewableServeListQryExe {

    @Resource
    private ElasticsearchTools elasticsearchTools;

    @Resource
    private OfficeAggregateRootApi officeAggregateRootApi;

    // 续约时查询的服务单的状态默认值，现在目前只能是已发车和维修中
    private final List<Integer> defaultServeStatus = Arrays.asList(ServeEnum.DELIVER.getCode(), ServeEnum.REPAIR.getCode());

    public List<ServeToRenewalVO> execute(RenewableServeQry qry, TokenInfo tokenInfo) {
        Result<List<SysOfficeDto>> officeCityListResult = officeAggregateRootApi.getOfficeCityListByRegionId(tokenInfo.getOfficeId());
        if (ResultStatusEnum.UNKNOWS.getCode() == officeCityListResult.getCode() || HttpStatus.INTERNAL_SERVER_ERROR.value() == officeCityListResult.getCode()) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), ResultErrorEnum.SERRVER_ERROR.getName());
        }
        if (OfficeCodeMsgEnum.OFFICE_NOT_EXIST.getCode() == officeCityListResult.getCode()) {
            throw new CommonException(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }

        // 查询条件拼装
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (OfficeCodeMsgEnum.OFFICE_LEVEL_HQ.getCode() != officeCityListResult.getCode()) {
            if (null == officeCityListResult.getData()) {
                throw new CommonException(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
            }
            List<Integer> orgIds = officeCityListResult.getData().stream().map(SysOfficeDto::getId).collect(Collectors.toList());
            boolQueryBuilder.must(QueryBuilders.termsQuery("orgId", orgIds));
        }
        boolQueryBuilder.must(QueryBuilders.termQuery("customerId", qry.getCustomerId()));
        if (null != qry.getCarId() && 0 != qry.getCarId()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("carId", qry.getCarId()));
        }
        if (!StringUtils.isEmpty(qry.getOaContractCode())) {
            boolQueryBuilder.must(QueryBuilders.termQuery("contractNo.keyword", qry.getOaContractCode()));
        }
        if (null != qry.getStatus() && 0 != qry.getStatus()) {
            if(!Objects.equals(ServeEnum.DELIVER.getCode(), qry.getStatus()) && !Objects.equals(ServeEnum.REPAIR.getCode(), qry.getStatus())){
                throw new CommonException(ResultErrorEnum.VILAD_ERROR.getCode(), "服务单状态传参错误");
            }
            boolQueryBuilder.must(QueryBuilders.termQuery("serveStatus", qry.getStatus()));
        } else {
            boolQueryBuilder.must(QueryBuilders.termsQuery("serveStatus", defaultServeStatus));
        }
        // 20220214新增，被续约的服务单不可是维修车的替换服务单
        boolQueryBuilder.must(QueryBuilders.termQuery("replaceFlag", JudgeEnum.NO.getCode()));

        // 排序条件拼装
        ArrayList<FieldSortBuilder> fieldSortList = new ArrayList<>();
        fieldSortList.add(SortBuilders.fieldSort("serveStatus").unmappedType("integer").order(SortOrder.ASC));
        fieldSortList.add(SortBuilders.fieldSort("deliverVehicleTime").unmappedType("date").order(SortOrder.ASC));

        Map<String, Object> map = elasticsearchTools.searchByQuerySort(DeliverUtils.getEnvVariable(Constants.ES_DELIVER_INDEX), Utils.getEnvVariable(Constants.ES_DELIVER_INDEX), 0, 0, boolQueryBuilder, fieldSortList);
        List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
        List<ServeToRenewalVO> serveToRenewalVOList = new LinkedList<>();
        for (Map<String, Object> serveMap : data) {
            ServeToRenewalVO serveToRenewalVO = new ServeToRenewalVO();
            ServeES serveEs = BeanUtil.mapToBean(serveMap, ServeES.class, false, new CopyOptions());
            BeanUtil.copyProperties(serveEs, serveToRenewalVO);
            serveToRenewalVO.setOaContractCode(serveEs.getContractNo());
            serveToRenewalVO.setBrandDisplay(serveEs.getBrandModelDisplay());
            serveToRenewalVO.setStatusDisplay(Objects.requireNonNull(ServeEnum.getServeEnum(serveEs.getServeStatus())).getStatus());
            if(null != serveEs.getDeliverVehicleTime()){
                Date nowDate = new Date();
                Date deliverVehicleTime = serveEs.getDeliverVehicleTime();
                serveToRenewalVO.setLeaseDays(String.valueOf(DateUtil.between(nowDate, deliverVehicleTime, DateUnit.DAY)));
            }

            serveToRenewalVOList.add(serveToRenewalVO);
        }

        return serveToRenewalVOList;
    }

}
