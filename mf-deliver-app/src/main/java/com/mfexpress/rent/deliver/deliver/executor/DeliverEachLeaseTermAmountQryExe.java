package com.mfexpress.rent.deliver.deliver.executor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.mfexpress.billing.customer.api.aggregate.SubBillItemAggregateRootApi;
import com.mfexpress.billing.customer.constant.CyclicBillPaymentStatusEnum;
import com.mfexpress.billing.customer.data.dto.billitem.SubBillItemDTO;
import com.mfexpress.billing.rentcharge.api.FeeAggregeateRootApi;
import com.mfexpress.billing.rentcharge.dto.data.fee.ServeLeaseTermInfoDTO;
import com.mfexpress.common.app.userCentre.dto.EmployeeDTO;
import com.mfexpress.common.app.userCentre.dto.qry.UserListByEmployeeIdsQry;
import com.mfexpress.common.domain.api.UserAggregateRootApi;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.LeaseModelEnum;
import com.mfexpress.rent.deliver.constant.ServeChangeRecordEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverEachLeaseTermAmountVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeChangeRecordDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryCmd;
import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeInfoVO;
import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeOperationRecordVO;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleDto;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.entity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DeliverEachLeaseTermAmountQryExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    // 子账单项聚合根
    @Resource
    private SubBillItemAggregateRootApi subBillItemAggregateRootApi;

    @Resource
    private FeeAggregeateRootApi feeAggregeateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    private Map<Integer, String> vehicleTypeMap;

    @Resource
    private UserAggregateRootApi userAggregateRootApi;

    public ServeInfoVO execute(ServeQryCmd qry) {
        initDictData();
        /*DeliverQry deliverQry = new DeliverQry();
        deliverQry.setStatus((Arrays.asList(DeliverStatusEnum.VALID.getCode(), DeliverStatusEnum.HISTORICAL.getCode())));
        deliverQry.setServeNo(qry.getServeNo());
        Result<List<DeliverDTO>> deliverDTOListResult = deliverAggregateRootApi.getDeliverListByQry(deliverQry);
        // 根据交付单查费项
        Result<DeliverDTO> deliverByServeNo = deliverAggregateRootApi.getDeliverByServeNo(qry.getServeNo());*/
        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(qry.getServeNo());
        ServeDTO serveDTO = ResultDataUtils.getInstance(serveDTOResult).getDataOrException();
        if (null == serveDTO) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "查询服务单失败");
        }

        // 客户查询
        Result<Customer> customerResult = customerAggregateRootApi.getCustomerById(serveDTO.getCustomerId());
        Customer customer = ResultDataUtils.getInstance(customerResult).getDataOrNull();

        Result<List<ServeLeaseTermInfoDTO>> serveLeaseTermInfoDTOListResult = feeAggregeateRootApi.getServeLeaseTermInfoByServeNo(qry.getServeNo());
        List<ServeLeaseTermInfoDTO> serveLeaseTermInfoDTOList = ResultDataUtils.getInstance(serveLeaseTermInfoDTOListResult).getDataOrNull();
//        if (null == serveLeaseTermInfoDTOList || serveLeaseTermInfoDTOList.isEmpty()) {
//            log.info("serveNo:{}租期数据查询失败或暂无租期数据", serveDTO.getServeNo());
//            return new ServeInfoVO();
//        }

        // 刨去租期为当月及以后月的服务单租期信息
        /*Date nowDate = new Date();
        serveLeaseTermInfoDTOList = serveLeaseTermInfoDTOList.stream().filter(p -> DateUtil.parse(p.getLeaseTerm(), "yyyy-MM").isBefore(nowDate)).collect(Collectors.toList());
        if(serveLeaseTermInfoDTOList.isEmpty()){
            log.info("serveNo:{}租期数据取过滤掉当前月及以后月的数据之后为空", serveDTO.getServeNo());
            return PagePagination.getInstance(new ArrayList<>());
        }*/

        List<Integer> vehicleIdList = new ArrayList<>();
        List<Long> detailIdList = serveLeaseTermInfoDTOList.stream().map(serveLeaseTermInfoDTO -> {
            vehicleIdList.add(serveLeaseTermInfoDTO.getVehicleId());
            return serveLeaseTermInfoDTO.getDetailId();
        }).collect(Collectors.toList());
        Result<Map<Long, SubBillItemDTO>> detailIdWithSubBillItemDTOMapResult = subBillItemAggregateRootApi.getSubBillItemByDetailList(detailIdList);
        Map<Long, SubBillItemDTO> detailIdWithSubBillItemDTOMap = ResultDataUtils.getInstance(detailIdWithSubBillItemDTOMapResult).getDataOrNull();
//        if (null == detailIdWithSubBillItemDTOMap || detailIdWithSubBillItemDTOMap.isEmpty()) {
//            log.error("查询租期费用失败，详单id：{}", detailIdList);
//            return new ServeInfoVO();
//        }
        Map<Long, List<SubBillItemDTO.SubBillItemRecordDTO>> subBillItemRecordDTOMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(detailIdWithSubBillItemDTOMap)) {
            List<String> subBillItemIdList = detailIdWithSubBillItemDTOMap.values().stream().map(subBillItemDTO -> String.valueOf(subBillItemDTO.getSubBillItemId())).collect(Collectors.toList());
            Result<List<SubBillItemDTO.SubBillItemRecordDTO>> subBillItemAdjustRecordResult = subBillItemAggregateRootApi.getSubBillItemAdjustRecord(subBillItemIdList);
            List<SubBillItemDTO.SubBillItemRecordDTO> subBillItemRecordDTOList = ResultDataUtils.getInstance(subBillItemAdjustRecordResult).getDataOrNull();
            if (null == subBillItemRecordDTOList || subBillItemRecordDTOList.isEmpty()) {
                log.error("查询调账金额失败，子帐项id：{}", subBillItemIdList);
            } else {
                subBillItemRecordDTOMap = subBillItemRecordDTOList.stream().collect(Collectors.groupingBy(SubBillItemDTO.SubBillItemRecordDTO::getSubBillItemId));
            }
        } else {
            detailIdWithSubBillItemDTOMap = new HashMap<>();
        }


        // 车牌号查询
        Result<List<VehicleDto>> vehicleDTOListResult = vehicleAggregateRootApi.getVehicleDTOByIds(vehicleIdList);
        List<VehicleDto> vehicleDtoList = ResultDataUtils.getInstance(vehicleDTOListResult).getDataOrNull();
        Map<Integer, VehicleDto> vehicleDtoMap = null;
        if (null != vehicleDtoList && !vehicleDtoList.isEmpty()) {
            vehicleDtoMap = vehicleDtoList.stream().collect(Collectors.toMap(VehicleDto::getId, Function.identity(), (v1, v2) -> v1));
        }

        Result<List<ServeChangeRecordDTO>> serveChangeRecordListResult = serveAggregateRootApi.getServeChangeRecordListByServeNo(qry.getServeNo());
        List<ServeChangeRecordDTO> serveChangeRecordDTOS = ResultDataUtils.getInstance(serveChangeRecordListResult).getDataOrNull().stream().filter(s ->
                s.getType().equals(ServeChangeRecordEnum.TERMINATION.getCode())
                        || s.getType().equals(ServeChangeRecordEnum.REACTIVE.getCode())
                        || s.getType().equals(ServeChangeRecordEnum.REPLACE_ADJUST.getCode())).collect(Collectors.toList());

        Map<Integer, VehicleDto> finalVehicleDtoMap = vehicleDtoMap;
        Map<Long, List<SubBillItemDTO.SubBillItemRecordDTO>> finalSubBillItemRecordDTOMap = subBillItemRecordDTOMap;
        Map<Long, SubBillItemDTO> finalDetailIdWithSubBillItemDTOMap = detailIdWithSubBillItemDTOMap;
        List<DeliverEachLeaseTermAmountVO> voList = serveLeaseTermInfoDTOList.stream().map(serveLeaseTermInfoDTO -> {
            Long detailId = serveLeaseTermInfoDTO.getDetailId();
            DeliverEachLeaseTermAmountVO deliverEachLeaseTermAmountVO = new DeliverEachLeaseTermAmountVO();
            deliverEachLeaseTermAmountVO.setCustomerId(serveDTO.getCustomerId());
            if (null != customer) {
                deliverEachLeaseTermAmountVO.setCustomerName(customer.getName());
            }
            deliverEachLeaseTermAmountVO.setOaContractCode(serveDTO.getOaContractCode());
            deliverEachLeaseTermAmountVO.setCarId(serveLeaseTermInfoDTO.getVehicleId());
            if (null != finalVehicleDtoMap) {
                VehicleDto vehicleDto = finalVehicleDtoMap.get(deliverEachLeaseTermAmountVO.getCarId());
                if (null != vehicleDto) {
                    deliverEachLeaseTermAmountVO.setPlateNumber(vehicleDto.getPlateNumber());
                    deliverEachLeaseTermAmountVO.setCarModelId(vehicleDto.getTypeId());
                    // 车型
                    deliverEachLeaseTermAmountVO.setCarModelDisplay(vehicleTypeMap.get(vehicleDto.getBrandId()));
                }
            }
            if (JudgeEnum.YES.getCode().equals(serveDTO.getReplaceFlag())) {
                deliverEachLeaseTermAmountVO.setLeaseModelId(LeaseModelEnum.REPLACEMENT.getCode());
            } else {
                deliverEachLeaseTermAmountVO.setLeaseModelId(serveDTO.getLeaseModelId());
            }
            LeaseModelEnum leaseModelEnum = LeaseModelEnum.getEnum(deliverEachLeaseTermAmountVO.getLeaseModelId());
            deliverEachLeaseTermAmountVO.setLeaseModelDisplay(leaseModelEnum == null ? null : leaseModelEnum.getName());
            deliverEachLeaseTermAmountVO.setRealTimeRentFee(serveLeaseTermInfoDTO.getRentFee().toString());
            deliverEachLeaseTermAmountVO.setLeaseMonth(serveLeaseTermInfoDTO.getLeaseTerm());
            deliverEachLeaseTermAmountVO.setLeaseMonthStartDay(serveLeaseTermInfoDTO.getStartDate());
            deliverEachLeaseTermAmountVO.setLeaseMonthEndDay(serveLeaseTermInfoDTO.getEndDate());
            deliverEachLeaseTermAmountVO.setLeaseMonthStartWithEndDay(serveLeaseTermInfoDTO.getStartDate() + " - " + serveLeaseTermInfoDTO.getEndDate());
            deliverEachLeaseTermAmountVO.setUnitPrice(serveLeaseTermInfoDTO.getUnitPrice().toString());

            // 回款状态和待还金额
            BigDecimal amount = BigDecimal.ZERO;
            SubBillItemDTO subBillItemDTO = finalDetailIdWithSubBillItemDTOMap.get(detailId);
            if (null != subBillItemDTO) {
                deliverEachLeaseTermAmountVO.setUnpaidAmount(subBillItemDTO.getUnpaidAmount().toString());
                deliverEachLeaseTermAmountVO.setRepaymentStatus(subBillItemDTO.getStatus());
                // 客户账单域没有合适的枚举类，故此处先写死
                if (0 == deliverEachLeaseTermAmountVO.getRepaymentStatus()) {
                    deliverEachLeaseTermAmountVO.setRepaymentStatusDisplay("待回款");
                } else if (1 == deliverEachLeaseTermAmountVO.getRepaymentStatus()) {
                    deliverEachLeaseTermAmountVO.setRepaymentStatusDisplay("部分回款");
                } else if (2 == deliverEachLeaseTermAmountVO.getRepaymentStatus()) {
                    deliverEachLeaseTermAmountVO.setRepaymentStatusDisplay("已回款");
                }

                /*CyclicBillPaymentStatusEnum paymentStatusEnum = CyclicBillPaymentStatusEnum.getCyclicBillPaymentStatusEnum(subBillItemDTO.getStatus());
                if (null != paymentStatusEnum) {

                    deliverEachLeaseTermAmountVO.setRepaymentStatusDisplay(paymentStatusEnum.getName());
                }*/

                // 累计调账金额
                if (null != finalSubBillItemRecordDTOMap) {
                    List<SubBillItemDTO.SubBillItemRecordDTO> subBillItemRecordDTOS = finalSubBillItemRecordDTOMap.get(subBillItemDTO.getSubBillItemId());
                    if (null != subBillItemRecordDTOS && !subBillItemRecordDTOS.isEmpty()) {
                        for (SubBillItemDTO.SubBillItemRecordDTO subBillItemRecordDTO : subBillItemRecordDTOS) {
                            amount = amount.add(subBillItemRecordDTO.getAdjustAmount());
                        }
                    }
                }
                deliverEachLeaseTermAmountVO.setTotalAdjustAmount(amount.toString());
            } else {
                deliverEachLeaseTermAmountVO.setUnpaidAmount(deliverEachLeaseTermAmountVO.getRealTimeRentFee());
                deliverEachLeaseTermAmountVO.setRepaymentStatus(CyclicBillPaymentStatusEnum.INIT.getCode());
                deliverEachLeaseTermAmountVO.setRepaymentStatusDisplay("待回款");
                deliverEachLeaseTermAmountVO.setTotalAdjustAmount(amount.toString());
            }

            return deliverEachLeaseTermAmountVO;
        }).collect(Collectors.toList());

        Map<Integer, EmployeeDTO> employeeDTOMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(serveChangeRecordDTOS)) {
            UserListByEmployeeIdsQry userListByEmployeeIdsQry = new UserListByEmployeeIdsQry();
            List<Integer> employeeIds = serveChangeRecordDTOS.stream().map(ServeChangeRecordDTO::getCreatorId).distinct().collect(Collectors.toList());
            StringBuilder saleIdStr = new StringBuilder();
            for (Integer saleId : employeeIds) {
                saleIdStr.append(saleId).append(",");
            }
            Result<List<EmployeeDTO>> employeeListByEmployees = userAggregateRootApi.getEmployeeListByEmployees(userListByEmployeeIdsQry);
            if (CollectionUtil.isNotEmpty(employeeListByEmployees.getData())) {
                employeeDTOMap = employeeListByEmployees.getData().stream().collect(Collectors.toMap(EmployeeDTO::getId, a -> a));
            }
        }
        List<ServeOperationRecordVO> serveOperationRecordVOS = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(serveChangeRecordDTOS)) {
            for (ServeChangeRecordDTO serveChangeRecordDTO : serveChangeRecordDTOS) {
                ServeOperationRecordVO serveOperationRecordVO = new ServeOperationRecordVO();
                serveOperationRecordVO.setOperationTime(DateUtil.formatDateTime(serveChangeRecordDTO.getCreateTime()));
                serveOperationRecordVO.setCategory(ServeChangeRecordEnum.getServeChangeRecordEnum(serveChangeRecordDTO.getType()).getName());
                serveOperationRecordVO.setOperator(employeeDTOMap.getOrDefault(serveChangeRecordDTO.getCreatorId(), new EmployeeDTO()).getNickName());
                serveOperationRecordVOS.add(serveOperationRecordVO);
            }
        }
        ServeInfoVO serveInfoVO = new ServeInfoVO();
        serveInfoVO.setData(voList);
        serveInfoVO.setRecordVOS(serveOperationRecordVOS);
        return serveInfoVO;

    }

    public void initDictData() {
        if (null == vehicleTypeMap) {
            Result<Map<Integer, String>> vehicleTypeResult = vehicleAggregateRootApi.getAllVehicleBrandTypeList();
            if (ResultErrorEnum.SUCCESSED.getCode() == vehicleTypeResult.getCode() && null != vehicleTypeResult.getData()) {
                vehicleTypeMap = vehicleTypeResult.getData();
            }
        }
    }

}
