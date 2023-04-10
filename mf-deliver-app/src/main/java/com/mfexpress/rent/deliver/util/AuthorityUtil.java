package com.mfexpress.rent.deliver.util;

import com.mfexpress.business.starter.common.constant.DataScopeEnum;
import com.mfexpress.business.starter.common.dto.DataScopeInfoDTO;
import com.mfexpress.business.starter.datascope.util.DataScopeThreadLocalUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.dto.data.ListQry;
import com.mfexpress.transportation.customer.api.RentalCustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.rent.RentalCustomerDTO;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthorityUtil {

    @Resource
    private RentalCustomerAggregateRootApi rentalCustomerAggregateRootApi;

    public boolean supplyAuthorityEsQuery(BoolQueryBuilder boolQueryBuilder) {
        DataScopeInfoDTO dataScopeInfoDTO = DataScopeThreadLocalUtil.get();
        if (null == dataScopeInfoDTO) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "权限数据获取失败");
        }

        Integer dataScope = dataScopeInfoDTO.getDataScope();
        if (DataScopeEnum.REGION.getCode().equals(dataScope) || DataScopeEnum.CITY.getCode().equals(dataScope)) {
            List<Integer> orgIdList = dataScopeInfoDTO.getOrgIdList();
            if (null == orgIdList || orgIdList.isEmpty()) {
                return false;
            }
            boolQueryBuilder.must(QueryBuilders.termsQuery("orgId", orgIdList));
        } else if (DataScopeEnum.GROUP.getCode().equals(dataScope) || DataScopeEnum.SELF.getCode().equals(dataScope)) {
            List<Integer> userIdList = dataScopeInfoDTO.getUserIdList();
            if (null == userIdList || userIdList.isEmpty()) {
                return false;
            }
            Result<List<RentalCustomerDTO>> customersResult = rentalCustomerAggregateRootApi.getBySaleIds(userIdList);
            List<RentalCustomerDTO> customers = ResultDataUtils.getInstance(customersResult).getDataOrNull();
            if (null == customers || customers.isEmpty()) {
                return false;
            }
            List<Integer> customerIds = customers.stream().map(RentalCustomerDTO::getId).collect(Collectors.toList());
            boolQueryBuilder.must(QueryBuilders.termsQuery("customerId", customerIds));
        }
        return true;
    }

    public boolean supplyAuthority(ListQry qry) {
        DataScopeInfoDTO dataScopeInfoDTO = DataScopeThreadLocalUtil.get();
        if (null == dataScopeInfoDTO) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "权限数据获取失败");
        }
        Integer dataScope = dataScopeInfoDTO.getDataScope();
        if (DataScopeEnum.REGION.getCode().equals(dataScope) || DataScopeEnum.CITY.getCode().equals(dataScope)) {
            List<Integer> orgIdList = dataScopeInfoDTO.getOrgIdList();
            if (null == orgIdList || orgIdList.isEmpty()) {
                return false;
            }
            qry.setOrgIds(orgIdList);
        } else if (DataScopeEnum.GROUP.getCode().equals(dataScope) || DataScopeEnum.SELF.getCode().equals(dataScope)) {
            List<Integer> userIdList = dataScopeInfoDTO.getUserIdList();
            if (null == userIdList || userIdList.isEmpty()) {
                return false;
            }
            Result<List<RentalCustomerDTO>> customersResult = rentalCustomerAggregateRootApi.getBySaleIds(userIdList);
            List<RentalCustomerDTO> customers = ResultDataUtils.getInstance(customersResult).getDataOrNull();
            if (null == customers || customers.isEmpty()) {
                return false;
            }
            List<Integer> customerIds = customers.stream().map(RentalCustomerDTO::getId).collect(Collectors.toList());
            qry.setCustomerIds(customerIds);
        }
        return true;
    }

}
