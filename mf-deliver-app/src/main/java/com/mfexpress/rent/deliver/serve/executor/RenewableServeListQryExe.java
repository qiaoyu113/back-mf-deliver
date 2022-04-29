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
import com.mfexpress.component.starter.tools.es.ElasticsearchTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.order.api.app.ContractAggregateRootApi;
import com.mfexpress.order.dto.data.CommodityDTO;
import com.mfexpress.order.dto.data.CommodityMapDTO;
import com.mfexpress.order.dto.data.InsuranceInfoDTO;
import com.mfexpress.order.dto.qry.CommodityMapQry;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.RenewableServeQry;
import com.mfexpress.rent.deliver.dto.data.serve.ServeChangeRecordDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeToRenewalVO;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class RenewableServeListQryExe {

    @Resource
    private ElasticsearchTools elasticsearchTools;

    @Resource
    private OfficeAggregateRootApi officeAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private ContractAggregateRootApi contractAggregateRootApi;

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
            if (!Objects.equals(ServeEnum.DELIVER.getCode(), qry.getStatus()) && !Objects.equals(ServeEnum.REPAIR.getCode(), qry.getStatus())) {
                throw new CommonException(ResultErrorEnum.VILAD_ERROR.getCode(), "服务单状态传参错误");
            }
            boolQueryBuilder.must(QueryBuilders.termQuery("serveStatus", qry.getStatus()));
        } else {
            boolQueryBuilder.must(QueryBuilders.termsQuery("serveStatus", defaultServeStatus));
        }
        if (null != qry.getLeaseMode() && !qry.getLeaseMode().isEmpty()) {
            // 如果前端只传leaseModel为3（展示），进行限制查询，其他的不限制
            if(qry.getLeaseMode().size() == 1 && qry.getLeaseMode().get(0).equals(3)){
                boolQueryBuilder.must(QueryBuilders.termsQuery("leaseModelId", qry.getLeaseMode()));
            }
        }
        boolQueryBuilder.mustNot(QueryBuilders.termQuery("deliverStatus", DeliverEnum.IS_RECOVER.getCode()));
        // 20220214新增，被续约的服务单不可是维修车的替换服务单
        boolQueryBuilder.must(QueryBuilders.termQuery("replaceFlag", JudgeEnum.NO.getCode()));

        // 排序条件拼装
        ArrayList<FieldSortBuilder> fieldSortList = new ArrayList<>();
        fieldSortList.add(SortBuilders.fieldSort("serveStatus").unmappedType("integer").order(SortOrder.ASC));
        fieldSortList.add(SortBuilders.fieldSort("deliverVehicleTime").unmappedType("date").order(SortOrder.ASC));

        Map<String, Object> map = elasticsearchTools.searchByQuerySort(DeliverUtils.getEnvVariable(Constants.ES_SERVE_INDEX), Utils.getEnvVariable(Constants.ES_SERVE_TYPE), 0, 0, boolQueryBuilder, fieldSortList);
        List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
        List<ServeToRenewalVO> serveToRenewalVOList = new LinkedList<>();
        if (data.isEmpty()) {
            return serveToRenewalVOList;
        }

        List<ServeES> serveESList = data.stream().map(dataMap -> BeanUtil.mapToBean(dataMap, ServeES.class, false, new CopyOptions())).collect(Collectors.toList());
        assembleServeToRenewalVO(serveESList, serveToRenewalVOList);

        return serveToRenewalVOList;
    }

    private void assembleServeToRenewalVO(List<ServeES> serveESList, List<ServeToRenewalVO> serveToRenewalVOList) {
        ArrayList<Integer> contractCommodityIdList = new ArrayList<>();

        // 判断serve是主动续签还是被动续签，找出其实际的goodsId
        serveESList.forEach(serveES -> {
            contractCommodityIdList.add(serveES.getContractCommodityId());
        });

        // 访问订单域，根据goodsId查出合同商品信息
        CommodityMapQry commodityMapQry = new CommodityMapQry();
        commodityMapQry.setContractCommodityIdList(contractCommodityIdList);
        Result<CommodityMapDTO> commodityMapResult = contractAggregateRootApi.getCommodityMapByQry(commodityMapQry);
        CommodityMapDTO commodityMapDTO = ResultDataUtils.getInstance(commodityMapResult).getDataOrNull();
        if (null == commodityMapDTO) {
            return;
        }
        Map<Integer, CommodityDTO> contractCommodityDTOMap = commodityMapDTO.getContractCommodityDTOMap();

        // 将查到的商品信息的部分属性设置到serveToRenewalVO中
        serveESList.forEach(serveES -> {
            ServeToRenewalVO serveToRenewalVO = new ServeToRenewalVO();
            BeanUtil.copyProperties(serveES, serveToRenewalVO);
            serveToRenewalVO.setExpectRecoverDate(serveES.getExpectRecoverDate());
            serveToRenewalVO.setPurpose(serveES.getLeaseModelId());
            serveToRenewalVO.setOaContractCode(serveES.getContractNo());
            serveToRenewalVO.setBrandDisplay(serveES.getBrandModelDisplay());
            serveToRenewalVO.setStatusDisplay(Objects.requireNonNull(ServeEnum.getServeEnum(serveES.getServeStatus())).getStatus());
            if (null != serveES.getDeliverVehicleTime()) {
                Date nowDate = new Date();
                Date deliverVehicleTime = serveES.getDeliverVehicleTime();
                if (nowDate.after(deliverVehicleTime)) {
                    serveToRenewalVO.setLeaseDays(String.valueOf(DateUtil.between(nowDate, deliverVehicleTime, DateUnit.DAY)));
                } else {
                    serveToRenewalVO.setLeaseDays("0");
                }

            }
            if (null != contractCommodityDTOMap) {
                CommodityDTO commodityDTO = contractCommodityDTOMap.get(serveES.getContractCommodityId());
                if (null != commodityDTO) {
                    serveToRenewalVO.setRentFee(commodityDTO.getRentFee() == null ? "0.00" : String.format("%.2f", commodityDTO.getRentFee()));
                    serveToRenewalVO.setServiceFee(commodityDTO.getServiceFee() == null ? "0.00" : String.format("%.2f", commodityDTO.getServiceFee()));
                    serveToRenewalVO.setDeposit(commodityDTO.getDepositFee() == null ? "0.00" : String.format("%.2f", commodityDTO.getDepositFee()));
                    InsuranceInfoDTO insuranceInfo = commodityDTO.getInsuranceInfo();
                    serveToRenewalVO.setInsuranceInfo(insuranceInfo);
                    serveToRenewalVO.setTags(insuranceInfo.getTags() == null ? new String[0] : insuranceInfo.getTags());

                }
            }
            serveToRenewalVOList.add(serveToRenewalVO);
        });
    }

    /*private void assembleServeToRenewalVO(List<ServeES> serveESList, List<ServeToRenewalVO> serveToRenewalVOList) {
        ArrayList<Integer> contractCommodityIdList = new ArrayList<>();
        ArrayList<Integer> orderCommodityIdList = new ArrayList<>();
        ArrayList<String> serveNoListWithContractCommodity = new ArrayList<>();
        ArrayList<String> serveNoListWithOrderCommodity = new ArrayList<>();

        // 判断serve是主动续签还是被动续签，找出其实际的goodsId
        serveESList.forEach(serveES -> {
            if (ServeRenewalTypeEnum.NOT.getCode().equals(serveES.getRenewalType())) {
                orderCommodityIdList.add(serveES.getGoodsId());
                serveNoListWithOrderCommodity.add(serveES.getServeNo());
            } else if (ServeRenewalTypeEnum.ACTIVE.getCode().equals(serveES.getRenewalType())) {
                contractCommodityIdList.add(serveES.getGoodsId());
                serveNoListWithContractCommodity.add(serveES.getServeNo());
            } else if (ServeRenewalTypeEnum.PASSIVE.getCode().equals(serveES.getRenewalType())) {
                // 如果是被动续签合同，需要找出此合同之前的续签状态是未续签还是主动续签
                Result<List<ServeChangeRecordDTO>> recordListResult = serveAggregateRootApi.getServeChangeRecordList(serveES.getServeNo());
                List<ServeChangeRecordDTO> recordList = ResultDataUtils.getInstance(recordListResult).getDataOrException();
                if (null == recordList || recordList.isEmpty()) {
                    return;
                }
                // 判断该服务单是否被主动续约过
                AtomicBoolean isActive = new AtomicBoolean(false);
                recordList.forEach(record -> {
                    if (ServeRenewalTypeEnum.ACTIVE.getCode().equals(record.getRenewalType())) {
                        isActive.set(true);
                    }
                });
                // 被主动续约过的服务单其goodsid是合同下商品的id
                if (isActive.get()) {
                    contractCommodityIdList.add(serveES.getGoodsId());
                    serveNoListWithContractCommodity.add(serveES.getServeNo());
                } else {
                    orderCommodityIdList.add(serveES.getGoodsId());
                    serveNoListWithOrderCommodity.add(serveES.getServeNo());
                }
            }
        });

        // 访问订单域，根据goodsId查出合同商品信息
        CommodityMapQry commodityMapQry = new CommodityMapQry();
        commodityMapQry.setContractCommodityIdList(contractCommodityIdList);
        commodityMapQry.setOrderCommodityIdList(orderCommodityIdList);
        Result<CommodityMapDTO> commodityMapResult = contractAggregateRootApi.getCommodityMapByQry(commodityMapQry);
        CommodityMapDTO commodityMapDTO = ResultDataUtils.getInstance(commodityMapResult).getDataOrNull();
        if (null == commodityMapDTO) {
            return;
        }
        Map<Integer, CommodityDTO> contractCommodityDTOMap = commodityMapDTO.getContractCommodityDTOMap();
        Map<Integer, CommodityDTO> orderCommodityDTOMap = commodityMapDTO.getOrderCommodityDTOMap();

        // 将查到的商品信息的部分属性设置到serveToRenewalVO中
        serveESList.forEach(serveES -> {
            ServeToRenewalVO serveToRenewalVO = new ServeToRenewalVO();
            BeanUtil.copyProperties(serveES, serveToRenewalVO);
            serveToRenewalVO.setExpectRecoverDate(serveES.getExpectRecoverDate());
            serveToRenewalVO.setPurpose(serveES.getLeaseModelId());
            serveToRenewalVO.setOaContractCode(serveES.getContractNo());
            serveToRenewalVO.setBrandDisplay(serveES.getBrandModelDisplay());
            serveToRenewalVO.setStatusDisplay(Objects.requireNonNull(ServeEnum.getServeEnum(serveES.getServeStatus())).getStatus());
            if (null != serveES.getDeliverVehicleTime()) {
                Date nowDate = new Date();
                Date deliverVehicleTime = serveES.getDeliverVehicleTime();
                if (nowDate.after(deliverVehicleTime)) {
                    serveToRenewalVO.setLeaseDays(String.valueOf(DateUtil.between(nowDate, deliverVehicleTime, DateUnit.DAY)));
                } else {
                    serveToRenewalVO.setLeaseDays("0");
                }

            }
            if (serveNoListWithContractCommodity.contains(serveES.getServeNo())) {
                if (null != contractCommodityDTOMap) {
                    CommodityDTO commodityDTO = contractCommodityDTOMap.get(serveES.getGoodsId());
                    if (null != commodityDTO) {
                        serveToRenewalVO.setRentFee(commodityDTO.getRentFee() == null ? "0.00" : commodityDTO.getRentFee().toString());
                        serveToRenewalVO.setServiceFee(commodityDTO.getServiceFee() == null ? "0.00" : commodityDTO.getServiceFee().toString());
                        InsuranceInfoDTO insuranceInfo = commodityDTO.getInsuranceInfo();
                        serveToRenewalVO.setInsuranceInfo(insuranceInfo);
                        serveToRenewalVO.setTags(insuranceInfo.getTags() == null ? new String[0] : insuranceInfo.getTags());

                    }
                }
            } else if (serveNoListWithOrderCommodity.contains(serveES.getServeNo())) {
                if (null != orderCommodityDTOMap) {
                    CommodityDTO commodityDTO = orderCommodityDTOMap.get(serveES.getGoodsId());
                    if (null != commodityDTO) {
                        serveToRenewalVO.setRentFee(commodityDTO.getRentFee() == null ? "0.00" : commodityDTO.getRentFee().toString());
                        serveToRenewalVO.setServiceFee(commodityDTO.getServiceFee() == null ? "0.00" : commodityDTO.getServiceFee().toString());
                        InsuranceInfoDTO insuranceInfo = commodityDTO.getInsuranceInfo();
                        serveToRenewalVO.setInsuranceInfo(insuranceInfo);
                        serveToRenewalVO.setTags(insuranceInfo.getTags() == null ? new String[0] : insuranceInfo.getTags());
                    }
                }
            }
            serveToRenewalVOList.add(serveToRenewalVO);
        });
    }*/

}
