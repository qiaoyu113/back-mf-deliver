package com.mfexpress.rent.deliver.serve.executor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageInfo;
import com.mfexpress.billing.customer.api.aggregate.SubBillItemAggregateRootApi;
import com.mfexpress.billing.customer.data.dto.billitem.SubBillItemDTO;
import com.mfexpress.billing.rentcharge.api.DetailAggregateRootApi;
import com.mfexpress.billing.rentcharge.dto.data.detail.DetailedByServeNoByLtLeaseTermDTO;
import com.mfexpress.common.app.userCentre.dto.EmployeeDTO;
import com.mfexpress.common.app.userCentre.dto.qry.UserListByEmployeeIdsQry;
import com.mfexpress.business.starter.common.dto.DataScopeInfoDTO;
import com.mfexpress.business.starter.datascope.util.DataScopeThreadLocalUtil;
import com.mfexpress.common.domain.api.OfficeAggregateRootApi;
import com.mfexpress.common.domain.api.UserAggregateRootApi;
import com.mfexpress.common.domain.dto.SysOfficeDto;
import com.mfexpress.common.domain.enums.OfficeCodeMsgEnum;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.response.ResultStatusEnum;
import com.mfexpress.component.starter.tools.es.ElasticsearchTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.order.api.app.ContractAggregateRootApi;
import com.mfexpress.order.dto.data.CommodityDTO;
import com.mfexpress.order.dto.data.CommodityMapDTO;
import com.mfexpress.order.dto.qry.CommodityMapQry;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAllLeaseTermAmountVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeLeaseTermAmountQry;
import com.mfexpress.rent.deliver.dto.es.ServeES;
import com.mfexpress.rent.deliver.util.AuthorityUtil;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import com.mfexpress.rent.deliver.utils.ServeDictDataUtil;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleDto;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.api.RentalCustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerEnterpriseNcInfoDTO;
import com.mfexpress.transportation.customer.dto.rent.RentalCustomerDTO;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ServeLeaseTermAmountQryExe {

    @Resource
    private OfficeAggregateRootApi officeAggregateRootApi;

    @Resource
    private ElasticsearchTools elasticsearchTools;

    // 费项详单聚合根
    @Resource
    private DetailAggregateRootApi detailAggregateRootApi;

    // 子账单项聚合根
    @Resource
    private SubBillItemAggregateRootApi subBillItemAggregateRootApi;

    @Resource
    private ContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private RentalCustomerAggregateRootApi rentalCustomerAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource
    private UserAggregateRootApi userAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private AuthorityUtil authorityUtil;

    @Resource
    private BeanFactory beanFactory;

    public PagePagination<ServeAllLeaseTermAmountVO> execute(ServeLeaseTermAmountQry qry, TokenInfo tokenInfo) {
        ServeDictDataUtil.initDictData(beanFactory);
        PageInfo<ServeAllLeaseTermAmountVO> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(qry.getPage());
        /*if (null != tokenInfo) {
            qry.setUserOfficeId(tokenInfo.getOfficeId());
        }*/
        DataScopeInfoDTO dataScopeInfoDTO = DataScopeThreadLocalUtil.get();
        if (null != dataScopeInfoDTO) {
            boolean userHasAuthorityFlag = authorityUtil.supplyAuthority(qry);
            if (!userHasAuthorityFlag) {
                pageInfo.setList(new ArrayList<>());
                return new PagePagination<>(pageInfo);
            }
        }

        BoolQueryBuilder boolQueryBuilder = assembleEsQryCondition(qry);
        List<FieldSortBuilder> fieldSortList = getSortConditions();

        if (0 >= qry.getLimit()) {
            qry.setLimit(10);
        }
        if (0 >= qry.getPage()) {
            qry.setPage(1);
        }
        int start = (qry.getPage() - 1) * qry.getLimit();
        Map<String, Object> resultMap = elasticsearchTools.searchByQuerySort(DeliverUtils.getEnvVariable(Constants.ES_SERVE_INDEX), Constants.ES_SERVE_TYPE, start, qry.getLimit(), boolQueryBuilder, fieldSortList);
        List<Map<String, Object>> data = (List<Map<String, Object>>) resultMap.get("data");
        long total = (long) resultMap.get("total");

        // 组合历史租期欠费情况等数据
        List<ServeAllLeaseTermAmountVO> voList = assembleData(data);

        BigDecimal bigDecimalTotal = new BigDecimal(total);
        BigDecimal bigDecimalLimit = new BigDecimal(qry.getLimit());
        BigDecimal pages = bigDecimalTotal.divide(bigDecimalLimit, RoundingMode.UP);

        pageInfo.setPages(pages.intValue());
        pageInfo.setTotal(total);
        pageInfo.setList(voList);

        return new PagePagination<>(pageInfo);
    }

    private List<ServeAllLeaseTermAmountVO> assembleData(List<Map<String, Object>> data) {
        List<ServeAllLeaseTermAmountVO> voList = new ArrayList<>();
        if (data.isEmpty()) {
            return voList;
        }
        Set<Integer> orgIdSet = new HashSet<>();
        List<String> serveNoList = new ArrayList<>();
        List<Integer> contractCommodityIdList = new ArrayList<>();
        List<Integer> customerIdList = new ArrayList<>();
        Set<Integer> historyVehicleIdSet = new HashSet<>();
        Set<Integer> saleIdSet = new HashSet<>();
        voList = data.stream().map(map -> {
            ServeAllLeaseTermAmountVO serveAllLeaseTermAmountVO = new ServeAllLeaseTermAmountVO();
            ServeES serveES = BeanUtil.mapToBean(map, ServeES.class, false, new CopyOptions());
            BeanUtils.copyProperties(serveES, serveAllLeaseTermAmountVO);
            if (!StringUtils.isEmpty(serveAllLeaseTermAmountVO.getDeposit())) {
                // 补充金额字段精度至小数点后两位
                serveAllLeaseTermAmountVO.setDeposit(supplementAccuracy(serveAllLeaseTermAmountVO.getDeposit()));
            }
            serveAllLeaseTermAmountVO.setPlateNumber(serveES.getCarNum());
            serveAllLeaseTermAmountVO.setCarModelDisplay(serveES.getBrandModelDisplay());
            serveAllLeaseTermAmountVO.setOaContractCode(serveES.getContractNo());
            serveAllLeaseTermAmountVO.setExpectRecoverDateChar(null == serveES.getExpectRecoverDate() ? null : DateUtil.format(serveES.getExpectRecoverDate(), "yyyy-MM-dd"));
            // 替换租赁方式单独设置
            if (JudgeEnum.YES.getCode().equals(serveES.getReplaceFlag())) {
                serveAllLeaseTermAmountVO.setLeaseModelId(LeaseModelEnum.REPLACEMENT.getCode());
                serveAllLeaseTermAmountVO.setLeaseModelDisplay(LeaseModelEnum.REPLACEMENT.getName());
            }
            // 所属管理区
            orgIdSet.add(serveAllLeaseTermAmountVO.getOrgId());
            serveNoList.add(serveAllLeaseTermAmountVO.getServeNo());
            contractCommodityIdList.add(serveES.getContractCommodityId());
            customerIdList.add(serveAllLeaseTermAmountVO.getCustomerId());
            // 历史车辆
            if (null != serveES.getHistoryVehicleIds() && !serveES.getHistoryVehicleIds().isEmpty()) {
                historyVehicleIdSet.addAll(serveES.getHistoryVehicleIds());
            }
            // 销售id
            saleIdSet.add(serveES.getSaleId());

            String rentStr = String.valueOf(!Objects.isNull(map.get("rent")) ? map.get("rent") : "0.00");
            String rentRatioStr = String.valueOf(!Objects.isNull(map.get("rentRatio")) ? map.get("rentRatio") : "0.00");
            BigDecimal rent = new BigDecimal(rentStr);
            BigDecimal rentRatio = new BigDecimal(rentRatioStr);
            serveAllLeaseTermAmountVO.setRentFee(String.format("%.2f", rent.multiply(rentRatio)));
            BigDecimal serviceFee = rent.subtract(rent.multiply(rentRatio));
            serveAllLeaseTermAmountVO.setServiceFee(String.format("%.2f", serviceFee));

            if (null != serveAllLeaseTermAmountVO.getVehicleBusinessMode()) {
                serveAllLeaseTermAmountVO.setVehicleBusinessModeDisplay(ServeDictDataUtil.vehicleBusinessModeMap.get(serveAllLeaseTermAmountVO.getVehicleBusinessMode().toString()));
            }
            serveAllLeaseTermAmountVO.setActualDeposit(serveES.getPaidInDeposit());
            return serveAllLeaseTermAmountVO;
        }).collect(Collectors.toList());

        // 数据查询 --------------------------- start
        Result<List<SysOfficeDto>> officeCityListResult = officeAggregateRootApi.getOfficeCityListByIdList(new ArrayList<>(orgIdSet));
        List<SysOfficeDto> sysOfficeDtoList = ResultDataUtils.getInstance(officeCityListResult).getDataOrNull();
        Map<Integer, SysOfficeDto> sysOfficeDtoMap = null;
        if (null != sysOfficeDtoList && !sysOfficeDtoList.isEmpty()) {
            sysOfficeDtoMap = sysOfficeDtoList.stream().collect(Collectors.toMap(SysOfficeDto::getId, Function.identity(), (v1, v2) -> v1));
        }

        List<RentalCustomerDTO> rentalCustomerDTOList = ResultDataUtils.getInstance(rentalCustomerAggregateRootApi.getRentalCustomerByCustomerIdList(customerIdList)).getDataOrNull();
        Map<Integer, RentalCustomerDTO> rentalCustomerDTOMap = CollUtil.isNotEmpty(rentalCustomerDTOList)
                ? rentalCustomerDTOList.stream().collect(Collectors.toMap(RentalCustomerDTO::getId, v -> v, (v1, v2) -> v1)) : new HashMap<>();

        List<CustomerEnterpriseNcInfoDTO> customerEnterpriseNcInfoDTOList = ResultDataUtils.getInstance(customerAggregateRootApi.getCustomerEnterpriseNcInfoDTOListByCustomerIdList(customerIdList)).getDataOrNull();
        Map<Integer, CustomerEnterpriseNcInfoDTO> customerEnterpriseNcInfoDTOMap = CollUtil.isNotEmpty(customerEnterpriseNcInfoDTOList)
                ? customerEnterpriseNcInfoDTOList.stream().collect(Collectors.toMap(CustomerEnterpriseNcInfoDTO::getCustomerId, v -> v, (v1, v2) -> v1)) : new HashMap<>();

        String saleIdString = rentalCustomerDTOList.stream().map(RentalCustomerDTO::getSaleId).map(String::valueOf).collect(Collectors.joining(","));
        UserListByEmployeeIdsQry userListByEmployeeIdsQry = new UserListByEmployeeIdsQry();
        userListByEmployeeIdsQry.setEmployeeIds(saleIdString);
        List<EmployeeDTO> employeeDTOList = ResultDataUtils.getInstance(userAggregateRootApi.getEmployeeListByEmployees(userListByEmployeeIdsQry)).getDataOrNull();
        Map<Integer, EmployeeDTO> employeeDTOMap = CollUtil.isNotEmpty(employeeDTOList)
                ? employeeDTOList.stream().collect(Collectors.toMap(EmployeeDTO::getId, v -> v, (v1, v2) -> v1)) : new HashMap<>();

        // 服务单 1 -----------> n 费项 1 -----------> 1 详单 1 -----------> 1 子账单项
        // 根据服务单号查询其下的多个详单id
        // 只查当月之前的数据，不含当月
        String nowYm = FormatUtil.ymFormatDateToString(new Date());
        DetailedByServeNoByLtLeaseTermDTO detailedByServeNoByLtLeaseTermDTO = new DetailedByServeNoByLtLeaseTermDTO();
        detailedByServeNoByLtLeaseTermDTO.setLeaseTerm(nowYm);
        detailedByServeNoByLtLeaseTermDTO.setServeNoList(serveNoList);
        Result<Map<String, List<Long>>> serveWithDetailIdMapResult = detailAggregateRootApi.getDetailIdByServeNoListAndLtLeaseTerm(detailedByServeNoByLtLeaseTermDTO);
        Map<String, List<Long>> serveWithDetailIdMap = ResultDataUtils.getInstance(serveWithDetailIdMapResult).getDataOrNull();
        Map<String, List<SubBillItemDTO>> serveNoWithSubBillItemDTOListMap = null;
        if (null != serveWithDetailIdMap && !serveWithDetailIdMap.isEmpty()) {
            // 倒排索引，以详单id为key，服务单号为value
            Map<Long, String> detailIdWithServeNoMap = new HashMap<>();
            serveWithDetailIdMap.keySet().forEach(serveNo -> {
                List<Long> detailIds = serveWithDetailIdMap.get(serveNo);
                detailIds.forEach(detailId -> {
                    detailIdWithServeNoMap.put(detailId, serveNo);
                });
            });

            // 得到所有的详单id
            List<Long> detailIdList = new ArrayList<>(detailIdWithServeNoMap.keySet());
            // 根据详单id去查子账单项，一对一的关系
            Result<Map<Long, SubBillItemDTO>> detailIdWithSubBillItemDTOResult = subBillItemAggregateRootApi.getSubBillItemByDetailList(detailIdList);
            Map<Long, SubBillItemDTO> detailIdWithSubBillItemDTOMap = ResultDataUtils.getInstance(detailIdWithSubBillItemDTOResult).getDataOrNull();

            // 将服务单号和其对应的子账单项列表组合成map
            if (null != detailIdWithSubBillItemDTOMap && !detailIdWithSubBillItemDTOMap.isEmpty()) {
                serveNoWithSubBillItemDTOListMap = new HashMap<>(serveWithDetailIdMap.size() + 1, 1L);
                for (Long detailId : detailIdList) {
                    SubBillItemDTO subBillItemDTO = detailIdWithSubBillItemDTOMap.get(detailId);
                    if (null != subBillItemDTO) {
                        String serveNo = detailIdWithServeNoMap.get(detailId);
                        List<SubBillItemDTO> subBillItemDTOS = serveNoWithSubBillItemDTOListMap.get(serveNo);
                        if (null == subBillItemDTOS) {
                            subBillItemDTOS = new ArrayList<>();
                            subBillItemDTOS.add(detailIdWithSubBillItemDTOMap.get(detailId));
                            serveNoWithSubBillItemDTOListMap.put(serveNo, subBillItemDTOS);
                        } else {
                            subBillItemDTOS.add(detailIdWithSubBillItemDTOMap.get(detailId));
                        }
                    }
                }
            }
        }

        // 根据合同商品id查询合同商品
        CommodityMapQry commodityMapQry = new CommodityMapQry();
        commodityMapQry.setContractCommodityIdList(contractCommodityIdList);
        Result<CommodityMapDTO> commodityMapResult = contractAggregateRootApi.getCommodityMapByQry(commodityMapQry);
        CommodityMapDTO commodityMapDTO = ResultDataUtils.getInstance(commodityMapResult).getDataOrNull();
        Map<Integer, CommodityDTO> contractCommodityDTOMap = null;
        if (null != commodityMapDTO && null != commodityMapDTO.getContractCommodityDTOMap() && !commodityMapDTO.getContractCommodityDTOMap().isEmpty()) {
            contractCommodityDTOMap = commodityMapDTO.getContractCommodityDTOMap();
        }


        //查询发车单
        List<String> serveNos = voList.stream().map(ServeAllLeaseTermAmountVO::getServeNo).distinct().collect(Collectors.toList());
        Result<List<DeliverVehicleDTO>> deliverVehicleResult = deliverVehicleAggregateRootApi.getDeliverVehicleByServeNoList(serveNos);
        List<DeliverVehicleDTO> deliverVehicleDTOS = ResultDataUtils.getInstance(deliverVehicleResult).getDataOrException();
        Map<String, List<DeliverVehicleDTO>> deliverVehicleMap = deliverVehicleDTOS.stream().collect(Collectors.groupingBy(DeliverVehicleDTO::getServeNo));

        Result<List<RecoverVehicleDTO>> recoverVehicleDTOResult = recoverVehicleAggregateRootApi.getRecoverVehicleDTOByServeNos(serveNos);
        List<RecoverVehicleDTO> recoverVehicleDTOS = ResultDataUtils.getInstance(recoverVehicleDTOResult).getDataOrException();
        Map<String, List<RecoverVehicleDTO>> recoverVehicleMap = recoverVehicleDTOS.stream().collect(Collectors.groupingBy(RecoverVehicleDTO::getServeNo));
        // 查询历史车辆
        Result<List<VehicleDto>> vehiclesResult = vehicleAggregateRootApi.getVehicleDTOByIds(new ArrayList<>(historyVehicleIdSet));
        List<VehicleDto> vehicles = vehiclesResult.getData();
        Map<Integer, VehicleDto> historyVehicleMap = null;
        if (null != vehicles && !vehicles.isEmpty()) {
            historyVehicleMap = vehicles.stream().collect(Collectors.toMap(VehicleDto::getVehicleId, Function.identity(), (v1, v2) -> v1));
        }
        // 查询销售
        String saleIdsStr = saleIdSet.stream().map(String::valueOf).collect(Collectors.joining(","));
        UserListByEmployeeIdsQry saleQry = new UserListByEmployeeIdsQry();
        saleQry.setEmployeeIds(saleIdsStr);
        Result<List<EmployeeDTO>> employeesResult = userAggregateRootApi.getEmployeeListByEmployees(saleQry);
        List<EmployeeDTO> employees = employeesResult.getData();
        Map<Integer, EmployeeDTO> employeeMap = null;
        if (null != employees && !employees.isEmpty()) {
            employeeMap = employees.stream().collect(Collectors.toMap(EmployeeDTO::getId, Function.identity(), (v1, v2) -> v1));
        }
        // 数据查询 --------------------------- end

        // 数据拼装 --------------------------- start
        for (ServeAllLeaseTermAmountVO vo : voList) {

            //发车日期 最近发车日期
            List<DeliverVehicleDTO> deliverVehicleDTOList = deliverVehicleMap.get(vo.getServeNo());
            if (CollectionUtil.isNotEmpty(deliverVehicleDTOList)) {
                List<DeliverVehicleDTO> deliverVehicleDTOList1 = deliverVehicleDTOList.stream().sorted(Comparator.comparing(DeliverVehicleDTO::getDeliverVehicleTime)).collect(Collectors.toList());
                vo.setFirstIssueDate(deliverVehicleDTOList1.get(0).getDeliverVehicleTime());
                vo.setFirstIssueDateChar(DateUtil.formatDate(deliverVehicleDTOList1.get(0).getDeliverVehicleTime()));
                vo.setRecentlyIssueDate(deliverVehicleDTOList1.get(deliverVehicleDTOList1.size() - 1).getDeliverVehicleTime());
                vo.setRecentlyIssueDateChar(DateUtil.formatDate(deliverVehicleDTOList1.get(deliverVehicleDTOList1.size() - 1).getDeliverVehicleTime()));
            }

            // 售后收车日期
            List<RecoverVehicleDTO> recoverVehicleDTOList = recoverVehicleMap.get(vo.getServeNo());
            if (CollectionUtil.isNotEmpty(recoverVehicleDTOList)) {
                List<RecoverVehicleDTO> recoverVehicleDTOS1 = recoverVehicleDTOList.stream().filter(r -> r.getRecoverVehicleTime() != null).sorted(Comparator.comparing(RecoverVehicleDTO::getRecoverVehicleTime)).collect(Collectors.toList());
                if (recoverVehicleDTOS1.size() > 0) {
                    vo.setRecentlyRecoverDate(recoverVehicleDTOS1.get(recoverVehicleDTOS1.size() - 1).getRecoverVehicleTime());
                    vo.setRecentlyRecoverDateChar(DateUtil.formatDate(recoverVehicleDTOS1.get(recoverVehicleDTOS1.size() - 1).getRecoverVehicleTime()));
                }
            }


            // 所属管理区名称补充
            if (null != sysOfficeDtoMap) {
                SysOfficeDto sysOfficeDto = sysOfficeDtoMap.get(vo.getOrgId());
                if (null != sysOfficeDto) {
                    vo.setOrgName(sysOfficeDto.getName());
                }
            }
            // 设置服务单状态含义补充
            ServeEnum serveEnum = ServeEnum.getServeEnum(vo.getServeStatus());
            if (null != serveEnum) {
                vo.setServeStatusDisplay(serveEnum.getAlias());
            }
            // 是否展示重新激活按钮 服务单状态为已收车 && 租赁方式为正常租赁或优惠 && 预计收车日期-收车日期>=15day && 预计收车日期在当前日期之后
            if (ServeEnum.RECOVER.getCode().equals(vo.getServeStatus()) && (LeaseModelEnum.NORMAL.getCode() == vo.getLeaseModelId() || LeaseModelEnum.DISCOUNT.getCode() == vo.getLeaseModelId() || LeaseModelEnum.SHOW.getCode() == vo.getLeaseModelId())) {
                if (null != vo.getRecoverVehicleTime() && null != vo.getExpectRecoverDate() && vo.getExpectRecoverDate().after(vo.getRecoverVehicleTime())) {
                    String nowDateChar = FormatUtil.ymdFormatDateToString(new Date());
                    Date nowDate = FormatUtil.ymdFormatStringToDate(nowDateChar);
                    if (!nowDate.after(vo.getExpectRecoverDate())) {
                        if (2 <= DateUtil.between(nowDate, vo.getExpectRecoverDate(), DateUnit.DAY)) {
                            vo.setEnableReactivate(JudgeEnum.YES.getCode());
                        }
                        /*long betweenDays = DateUtil.between(vo.getRecoverVehicleTime(), vo.getExpectRecoverDate(), DateUnit.DAY);
                        if (betweenDays >= 15) {
                            vo.setEnableReactivate(JudgeEnum.YES.getCode());
                        }*/
                    }
                }
            }

            // 非替换车租金、服务费补充
            if (LeaseModelEnum.REPLACEMENT.getCode() != vo.getLeaseModelId() && null != contractCommodityDTOMap) {
                CommodityDTO commodityDTO = contractCommodityDTOMap.get(vo.getContractCommodityId());
                if (null != commodityDTO) {
                    vo.setRentFee(supplementAccuracy(String.valueOf(commodityDTO.getRentFee())));
                    vo.setServiceFee(supplementAccuracy(String.valueOf(commodityDTO.getServiceFee())));
                }
            }

            // 历史租期欠费补充
            BigDecimal unpaidAmount = BigDecimal.ZERO;
            if (null != serveNoWithSubBillItemDTOListMap) {
                List<SubBillItemDTO> subBillItemDTOList = serveNoWithSubBillItemDTOListMap.get(vo.getServeNo());
                if (null != subBillItemDTOList && !subBillItemDTOList.isEmpty()) {
                    for (SubBillItemDTO subBillItemDTO : subBillItemDTOList) {
                        if (null != subBillItemDTO.getUnpaidAmount()) {
                            unpaidAmount = unpaidAmount.add(subBillItemDTO.getUnpaidAmount());
                        }
                    }
                }
            }
            vo.setTotalArrears(supplementAccuracy(unpaidAmount.toString()));

            String customerIDCardOrgSaleName = vo.getCustomerName();
            CustomerEnterpriseNcInfoDTO customerEnterpriseNcInfoDTO = customerEnterpriseNcInfoDTOMap.getOrDefault(vo.getCustomerId(), null);
            if (customerEnterpriseNcInfoDTO != null) {
                String creditCode = customerEnterpriseNcInfoDTO.getCreditCode();
                if (StrUtil.isNotEmpty(creditCode) && creditCode.length() >= 6) {
                    customerIDCardOrgSaleName += "(**" + creditCode.substring(creditCode.length() - 6, creditCode.length()) + ")";
                }
            }

            SysOfficeDto sysOfficeDto = sysOfficeDtoMap.getOrDefault(vo.getOrgId(), null);
            if (sysOfficeDto != null) {
                customerIDCardOrgSaleName += "-" + sysOfficeDto.getName();
            }

            RentalCustomerDTO rentalCustomerDTO = rentalCustomerDTOMap.getOrDefault(vo.getCustomerId(), null);
            if (ObjectUtil.isNotEmpty(rentalCustomerDTO)) {
                EmployeeDTO employeeDTO = employeeDTOMap.getOrDefault(rentalCustomerDTO.getSaleId(), null);
                if (ObjectUtil.isNotEmpty(employeeDTO)) {
                    customerIDCardOrgSaleName += "-" + employeeDTO.getNickName();
                }
            }

            vo.setCustomerIDCardOrgSaleName(customerIDCardOrgSaleName);
            vo.setCustomerName(customerIDCardOrgSaleName);
//            for (ServeAllLeaseTermAmountVO serveAllLeaseTermAmountVO : voList) {
//            }
            // 历史租期车辆
            if (null != historyVehicleMap) {
                List<Integer> historyVehicleIds = vo.getHistoryVehicleIds();
                if (null != historyVehicleIds) {
                    List<String> historyVehiclePlates = new ArrayList<>();
                    for (Integer historyVehicleId : historyVehicleIds) {
                        VehicleDto vehicle = historyVehicleMap.get(historyVehicleId);
                        if (null != vehicle) {
                            historyVehiclePlates.add(vehicle.getPlateNumber());
                        }
                    }
                    if (!historyVehiclePlates.isEmpty()) {
                        vo.setHistoryVehiclePlate(historyVehiclePlates);
                    }
                    vo.setHistoryVehiclePlate(historyVehiclePlates);
                }
            }

            // 所属销售
            if (null != employeeMap) {
                EmployeeDTO employeeDTO = employeeMap.get(vo.getSaleId());
                if (null != employeeDTO) {
                    vo.setSalesPersonName(employeeDTO.getNickName());
                }
            }
            // 客户类别
            Map<String, String> customerCategoryDictMap = ServeDictDataUtil.customerCategoryDictMap;
            if (null != customerCategoryDictMap) {
                if (null != vo.getCustomerCategory()) {
                    vo.setCustomerCategoryDisplay(customerCategoryDictMap.get(vo.getCustomerCategory().toString()));
                }
            }
            // 签约类型
            Map<String, String> signedTypeMap = ServeDictDataUtil.signedTypeDictMap;
            if (null != signedTypeMap) {
                if (null != vo.getSignedType()) {
                    vo.setSignedTypeDisplay(signedTypeMap.get(vo.getSignedType().toString()));
                }
            }
            // 租赁期限
            if (null != vo.getLeaseMonths() && 0 != vo.getLeaseMonths()) {
                vo.setLeaseTermDisplay(vo.getLeaseMonths() + "个月");
            } else {
                vo.setLeaseTermDisplay(vo.getLeaseDays() + "天");
            }
            // 业务类型
            if (null != vo.getBusinessType()) {
                ServeContractTemplateEnum templateEnum = ServeContractTemplateEnum.getByBusinessType(vo.getBusinessType());
                if (null != templateEnum) {
                    vo.setBusinessTypeDisplay(templateEnum.getDesc());
                }
            }
        }
        // 数据拼装 --------------------------- end

        return voList;
    }

    private List<FieldSortBuilder> getSortConditions() {
        List<FieldSortBuilder> fieldSortList = new ArrayList<>();
        fieldSortList.add(SortBuilders.fieldSort("serveStatusSort").unmappedType("integer").order(SortOrder.ASC));
        fieldSortList.add(SortBuilders.fieldSort("updateTime").unmappedType("date").order(SortOrder.DESC));
        return fieldSortList;
    }

    private BoolQueryBuilder assembleEsQryCondition(ServeLeaseTermAmountQry qry) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        /*if (null == qry.getOrgId() || 0 == qry.getOrgId()) {
            Result<List<SysOfficeDto>> officeCityListResult = officeAggregateRootApi.getOfficeCityListByRegionId(qry.getUserOfficeId());
            if (ResultStatusEnum.UNKNOWS.getCode() == officeCityListResult.getCode() || HttpStatus.INTERNAL_SERVER_ERROR.value() == officeCityListResult.getCode()) {
                throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), ResultErrorEnum.SERRVER_ERROR.getName());
            }
            if (OfficeCodeMsgEnum.OFFICE_NOT_EXIST.getCode() == officeCityListResult.getCode()) {
                throw new CommonException(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
            }

            // 如果组织级别不为总部，进行权限判断
            if (OfficeCodeMsgEnum.OFFICE_LEVEL_HQ.getCode() != officeCityListResult.getCode()) {
                if (null == officeCityListResult.getData()) {
                    throw new CommonException(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
                }
                List<Integer> orgIds = officeCityListResult.getData().stream().map(SysOfficeDto::getId).collect(Collectors.toList());
                boolQueryBuilder.must(QueryBuilders.termsQuery("orgId", orgIds));
            }
        } else {
            boolQueryBuilder.must(QueryBuilders.termQuery("orgId", qry.getOrgId()));
        }*/

        if (null != qry.getOrgIds() && !qry.getOrgIds().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.termsQuery("orgId", qry.getOrgIds()));
        } else if (null != qry.getCustomerIds() && !qry.getCustomerIds().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.termsQuery("customerId", qry.getCustomerIds()));
        }

        if (null != qry.getOrgId() && 0 != qry.getOrgId()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("orgId", qry.getOrgId()));
        }

        if (null != qry.getCustomerId() && 0 != qry.getCustomerId()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("customerId", qry.getCustomerId()));
        }

        if (null != qry.getCarId() && 0 != qry.getCarId()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("carId", qry.getCarId()));
        }

        if (null != qry.getCarModelId() && 0 != qry.getCarModelId()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("carModelId", qry.getCarModelId()));
        }

        if (null != qry.getLeaseModelId() && 0 != qry.getLeaseModelId() && 5 != qry.getLeaseModelId()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("leaseModelId", qry.getLeaseModelId()));
            boolQueryBuilder.must(QueryBuilders.termQuery("replaceFlag", JudgeEnum.NO.getCode()));
        }

        if (null != qry.getLeaseModelId() && 5 == qry.getLeaseModelId()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("replaceFlag", JudgeEnum.YES.getCode()));
        }

        if (!StringUtils.isEmpty(qry.getOaContractNo())) {
            boolQueryBuilder.must(QueryBuilders.termQuery("contractNo.keyword", qry.getOaContractNo()));
        }

        if (null != qry.getServeStatus()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("serveStatus", qry.getServeStatus()));
        }
        if (null != qry.getVehicleBusinessMode() && 0 != qry.getVehicleBusinessMode()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("vehicleBusinessMode", qry.getVehicleBusinessMode()));
        }
        if (null != qry.getExpectRecoverDateStart() && null != qry.getExpectRecoverDateEnd()) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery("expectRecoverDate").from(qry.getExpectRecoverDateStart().getTime()).to(qry.getExpectRecoverDateEnd().getTime()));
        }

        if (!StringUtils.isEmpty(qry.getServeNo())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("serveNo", qry.getServeNo()));
        }
        if (null != qry.getCustomerCategory() && 0 != qry.getCustomerCategory()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("customerCategory", qry.getCustomerCategory()));
        }
        if (null != qry.getSaleId() && 0 != qry.getSaleId()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("saleId", qry.getSaleId()));
        }
        if (null != qry.getHistoryVehicleId() && 0 != qry.getHistoryVehicleId()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("historyVehicleIds", qry.getHistoryVehicleId()));
        }
        if (!StringUtils.isEmpty(qry.getLeaseTerm())) {
            String[] split = qry.getLeaseTerm().split("-");
            RangeQueryBuilder queryBuilder = QueryBuilders.rangeQuery("totalLeaseDays").gte(split[0]);
            if (split.length == 2) {
                queryBuilder.lte(split[1]);
            }
            boolQueryBuilder.must(queryBuilder);
        }
        if (null != qry.getSignedType() && 0 != qry.getSignedType()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("signedType", qry.getSignedType()));
        }
        if (null != qry.getBusinessType() && 0 != qry.getBusinessType()) {
            boolQueryBuilder.must(QueryBuilders.termQuery("businessType", qry.getBusinessType()));
        }
        if (null != qry.getFirstDeliverVehicleDateStart() && null != qry.getFirstDeliverVehicleDateEnd()) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery("firstDeliverVehicleDate").from(qry.getFirstDeliverVehicleDateStart()).to(qry.getFirstDeliverVehicleDateEnd()));
        }
        if (null != qry.getRecentlyRecoverVehicleDateStart() && null != qry.getRecentlyRecoverVehicleDateEnd()) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery("recentlyRecoverVehicleDate").from(qry.getRecentlyRecoverVehicleDateStart()).to(qry.getRecentlyRecoverVehicleDateEnd()));
        }
        if (null != qry.getExpectRecoverDateStart() && null != qry.getExpectRecoverDateEnd()) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery("expectRecoverDate").from(qry.getExpectRecoverDateStart().getTime()).to(qry.getExpectRecoverDateEnd().getTime()));
        }
        return boolQueryBuilder;
    }

    // 补充精度至小数点后两位
    public String supplementAccuracy(String num) {
        if (StringUtils.isEmpty(num)) {
            return "0.00";
        }
        return new BigDecimal(num).setScale(2, RoundingMode.HALF_UP).toString();
    }
}
