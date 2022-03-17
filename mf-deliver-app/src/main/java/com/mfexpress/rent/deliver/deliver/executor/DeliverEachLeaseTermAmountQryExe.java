package com.mfexpress.rent.deliver.deliver.executor;

import com.mfexpress.billing.customer.api.aggregate.SubBillItemAggregateRootApi;
import com.mfexpress.billing.customer.constant.CyclicBillPaymentStatusEnum;
import com.mfexpress.billing.customer.data.dto.billitem.SubBillItemDTO;
import com.mfexpress.billing.rentcharge.api.DetailAggregateRootApi;
import com.mfexpress.billing.rentcharge.api.FeeAggregeateRootApi;
import com.mfexpress.billing.rentcharge.dto.data.fee.ServeLeaseTermInfoDTO;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.DeliverStatusEnum;
import com.mfexpress.rent.deliver.constant.LeaseModelEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverEachLeaseTermAmountVO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverQry;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryCmd;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleDto;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.entity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
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

    public PagePagination<DeliverEachLeaseTermAmountVO> execute(ServeQryCmd qry) {
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
        if (null == serveLeaseTermInfoDTOList || serveLeaseTermInfoDTOList.isEmpty()) {
            log.info("serveNo:{}租期数据查询失败或暂无租期数据", serveDTO.getServeNo());
            return PagePagination.getInstance(new ArrayList<>());
        }
        List<Integer> vehicleIdList = new ArrayList<>();
        List<Long> detailIdList = serveLeaseTermInfoDTOList.stream().map(serveLeaseTermInfoDTO -> {
            vehicleIdList.add(serveLeaseTermInfoDTO.getVehicleId());
            return serveLeaseTermInfoDTO.getDetailId();
        }).collect(Collectors.toList());
        Result<Map<Long, SubBillItemDTO>> detailIdWithSubBillItemDTOMapResult = subBillItemAggregateRootApi.getSubBillItemByDetailList(detailIdList);
        Map<Long, SubBillItemDTO> detailIdWithSubBillItemDTOMap = ResultDataUtils.getInstance(detailIdWithSubBillItemDTOMapResult).getDataOrNull();
        if (null == detailIdWithSubBillItemDTOMap || detailIdWithSubBillItemDTOMap.isEmpty()) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "查询租期费用失败");
        }
        List<String> subBillItemIdList = detailIdWithSubBillItemDTOMap.values().stream().map(subBillItemDTO -> String.valueOf(subBillItemDTO.getSubBillItemId())).collect(Collectors.toList());
        Result<List<SubBillItemDTO.SubBillItemRecordDTO>> subBillItemAdjustRecordResult = subBillItemAggregateRootApi.getSubBillItemAdjustRecord(subBillItemIdList);
        List<SubBillItemDTO.SubBillItemRecordDTO> subBillItemRecordDTOList = ResultDataUtils.getInstance(subBillItemAdjustRecordResult).getDataOrNull();
        Map<Long, List<SubBillItemDTO.SubBillItemRecordDTO>> subBillItemRecordDTOMap = null;
        if (null == subBillItemRecordDTOList || subBillItemRecordDTOList.isEmpty()) {
            log.error("查询调账金额失败，子帐项id：{}", subBillItemIdList);
        }else{
            subBillItemRecordDTOMap = subBillItemRecordDTOList.stream().collect(Collectors.groupingBy(SubBillItemDTO.SubBillItemRecordDTO::getSubBillItemId));
        }

        // 车牌号查询
        Result<List<VehicleDto>> vehicleDTOListResult = vehicleAggregateRootApi.getVehicleDTOByIds(vehicleIdList);
        List<VehicleDto> vehicleDtoList = ResultDataUtils.getInstance(vehicleDTOListResult).getDataOrNull();
        Map<Integer, VehicleDto> vehicleDtoMap = null;
        if (null != vehicleDtoList && !vehicleDtoList.isEmpty()) {
            vehicleDtoMap = vehicleDtoList.stream().collect(Collectors.toMap(VehicleDto::getId, Function.identity(), (v1, v2) -> v1));
        }

        Map<Integer, VehicleDto> finalVehicleDtoMap = vehicleDtoMap;
        Map<Long, List<SubBillItemDTO.SubBillItemRecordDTO>> finalSubBillItemRecordDTOMap = subBillItemRecordDTOMap;
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
                    deliverEachLeaseTermAmountVO.setModelId(vehicleDto.getTypeId());
                    // 车型
                    //deliverEachLeaseTermAmountVO.setModelDisplay();
                }

            }
            deliverEachLeaseTermAmountVO.setLeaseModelId(serveDTO.getLeaseModelId());
            LeaseModelEnum leaseModelEnum = LeaseModelEnum.getEnum(deliverEachLeaseTermAmountVO.getLeaseModelId());
            deliverEachLeaseTermAmountVO.setLeaseModelDisplay(leaseModelEnum == null ? null : leaseModelEnum.getName());
            deliverEachLeaseTermAmountVO.setRent(serveLeaseTermInfoDTO.getRentFee().toString());
            deliverEachLeaseTermAmountVO.setLeaseMonth(serveLeaseTermInfoDTO.getLeaseTerm());
            deliverEachLeaseTermAmountVO.setLeaseMonthStartDay(serveLeaseTermInfoDTO.getStartDate());
            deliverEachLeaseTermAmountVO.setLeaseMonthEndDay(serveLeaseTermInfoDTO.getEndDate());
            deliverEachLeaseTermAmountVO.setLeaseMonthStartWithEndDay(serveLeaseTermInfoDTO.getStartDate() + " - " + serveLeaseTermInfoDTO.getEndDate());
            deliverEachLeaseTermAmountVO.setUnitPrice(serveLeaseTermInfoDTO.getUnitPrice().toString());

            // 回款状态和待还金额
            SubBillItemDTO subBillItemDTO = detailIdWithSubBillItemDTOMap.get(detailId);
            if (null != subBillItemDTO) {
                deliverEachLeaseTermAmountVO.setUnpaidAmount(subBillItemDTO.getUnpaidAmount().toString());
                deliverEachLeaseTermAmountVO.setRepaymentStatus(subBillItemDTO.getStatus());
                CyclicBillPaymentStatusEnum paymentStatusEnum = CyclicBillPaymentStatusEnum.getCyclicBillPaymentStatusEnum(subBillItemDTO.getStatus());
                if (null != paymentStatusEnum) {
                    deliverEachLeaseTermAmountVO.setRepaymentStatusDisplay(paymentStatusEnum.getName());
                }
            }

            // 累计调账金额
            if(null != finalSubBillItemRecordDTOMap){
                List<SubBillItemDTO.SubBillItemRecordDTO> subBillItemRecordDTOS = finalSubBillItemRecordDTOMap.get(detailId);
                if (null != subBillItemRecordDTOS && !subBillItemRecordDTOS.isEmpty()) {
                    BigDecimal amount = BigDecimal.ZERO;
                    for (SubBillItemDTO.SubBillItemRecordDTO subBillItemRecordDTO : subBillItemRecordDTOS) {
                        amount = amount.add(subBillItemRecordDTO.getAdjustAmount());
                    }
                    deliverEachLeaseTermAmountVO.setTotalAdjustAmount(amount.toString());
                }
            }

            return deliverEachLeaseTermAmountVO;
        }).collect(Collectors.toList());

        return PagePagination.getInstance(voList);
    }

}
