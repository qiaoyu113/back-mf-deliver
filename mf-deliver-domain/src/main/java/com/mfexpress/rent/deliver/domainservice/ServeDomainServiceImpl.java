package com.mfexpress.rent.deliver.domainservice;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.github.pagehelper.PageInfo;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositListDTO;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositLockListDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDepositDTO;
import com.mfexpress.rent.deliver.entity.api.DeliverEntityApi;
import com.mfexpress.rent.deliver.entity.api.DeliverVehicleEntityApi;
import com.mfexpress.rent.deliver.entity.api.RecoverVehicleEntityApi;
import com.mfexpress.rent.deliver.entity.api.ServeEntityApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ServeDomainServiceImpl implements ServeDomainServiceI {

    @Resource
    private ServeEntityApi serveEntityApi;
    @Resource
    private DeliverEntityApi deliverEntityApi;
    @Resource
    private DeliverVehicleEntityApi deliverVehicleEntityApi;
    @Resource
    private RecoverVehicleEntityApi recoverVehicleEntityApi;

    @Override
    public PagePagination<ServeDepositDTO> getPageServeDeposit(CustomerDepositListDTO customerDepositListDTO) {
        //查询条件不包括车牌号
        if (Objects.isNull(customerDepositListDTO.getCarId()) || customerDepositListDTO.getCarId() == 0) {
            return getServeDepositByQry(customerDepositListDTO);
        } else {
            return getServeDepositByCarId(customerDepositListDTO);
        }
    }

    @Override
    public List<CustomerDepositLockListDTO> getCustomerDepositLockList(List<String> serveNoList) {
        List<CustomerDepositLockListDTO> depositLockListDTOS = new ArrayList<>(serveNoList.size());
        //查询服务单列表
        List<ServeDTO> serveDTOList = serveEntityApi.getServeListByServeNoList(serveNoList);
        if (CollectionUtil.isEmpty(serveDTOList)) {
            return CollectionUtil.newArrayList();
        }
        List<DeliverDTO> deliverDTOList = deliverEntityApi.getDeliverDTOListByServeNoList(serveNoList);
        List<String> deliverNoList = deliverDTOList.stream().map(DeliverDTO::getDeliverNo).collect(Collectors.toList());
        Map<String, DeliverDTO> deliverMap = deliverDTOList.stream().collect(Collectors.toMap(DeliverDTO::getServeNo, Function.identity()));
        //发车单列表
        List<DeliverVehicleDTO> deliverVehicleList = deliverVehicleEntityApi.getDeliverVehicleListByDeliverNoList(deliverNoList);
        Map<String, DeliverVehicleDTO> deliverVehicleMap = deliverVehicleList.stream().collect(Collectors.toMap(DeliverVehicleDTO::getDeliverNo, Function.identity()));

        for (ServeDTO serveDTO : serveDTOList) {
            CustomerDepositLockListDTO lockListDTO = new CustomerDepositLockListDTO();
            lockListDTO.setServeNo(serveDTO.getServeNo());
            lockListDTO.setBrandId(serveDTO.getBrandId());
            lockListDTO.setModelId(serveDTO.getCarModelId());
            lockListDTO.setPayableDeposit(serveDTO.getPayableDeposit());
            lockListDTO.setPaidInDeposit(serveDTO.getPaidInDeposit());
            DeliverDTO deliverDTO = deliverMap.get(serveDTO.getServeNo());
            if (Objects.isNull(deliverDTO)) {
                lockListDTO.setVehicleNum("");
                lockListDTO.setDeliverVehicleDate("");
            } else {
                lockListDTO.setVehicleNum(deliverDTO.getCarNum());
            }
            DeliverVehicleDTO deliverVehicleDTO = deliverVehicleMap.get(deliverDTO.getDeliverNo());
            if (Objects.isNull(deliverVehicleDTO)) {
                lockListDTO.setDeliverVehicleDate("");
            } else {
                lockListDTO.setDeliverVehicleDate(DateUtil.formatDate(deliverVehicleDTO.getDeliverVehicleTime()));
            }
            depositLockListDTOS.add(lockListDTO);

        }
        return depositLockListDTOS;

    }


    private PagePagination<ServeDepositDTO> getServeDepositByQry(CustomerDepositListDTO customerDepositLisDTO) {
        PagePagination<ServeDepositDTO> depositPagePagination = serveEntityApi.getServeDepositByQry(customerDepositLisDTO);
        List<ServeDepositDTO> serveDepositDTOList = depositPagePagination.getList();

        List<String> serveNoList = serveDepositDTOList.stream().map(ServeDepositDTO::getServeNo).collect(Collectors.toList());
        //查询有效得交付单
        List<DeliverDTO> deliverList = deliverEntityApi.getDeliverDTOListByServeNoList(serveNoList);
        Map<String, DeliverDTO> deliverMap = deliverList.stream().collect(Collectors.toMap(DeliverDTO::getServeNo, Function.identity()));
        //交付单编号
        List<String> deliverNoList = deliverList.stream().map(DeliverDTO::getDeliverNo).collect(Collectors.toList());
        //存在费用未确认得交付单
        List<DeliverDTO> deliverNotCompleteList = deliverEntityApi.getDeliverNotComplete(serveNoList);
        Map<String, List<DeliverDTO>> deliverNotCompletedMap = deliverNotCompleteList.stream().collect(Collectors.groupingBy(DeliverDTO::getServeNo));

        //发车单
        List<DeliverVehicleDTO> deliverVehicleList = deliverVehicleEntityApi.getDeliverVehicleListByDeliverNoList(deliverNoList);
        Map<String, DeliverVehicleDTO> deliverVehicleMap = deliverVehicleList.stream().collect(Collectors.toMap(DeliverVehicleDTO::getDeliverNo, Function.identity()));
        //收车单
        List<RecoverVehicleDTO> recoverVehicleList = recoverVehicleEntityApi.getRecoverListByDeliverNoList(deliverNoList);
        Map<String, RecoverVehicleDTO> recoverVehicleMap = recoverVehicleList.stream().collect(Collectors.toMap(RecoverVehicleDTO::getDeliverNo, Function.identity()));

        for (ServeDepositDTO serveDepositDTO : serveDepositDTOList) {
            //补充车牌号、收发车时间、收车费用确认情况
            DeliverDTO deliverDTO = deliverMap.get(serveDepositDTO.getServeNo());
            serveDepositDTO.setStatusDisplay(ServeEnum.getServeEnum(serveDepositDTO.getStatus()).getStatus());
            if (Objects.isNull(deliverDTO)) {
                log.warn("交付单不存在，服务单编号：{},服务单状态，{}", serveDepositDTO.getServeNo(), serveDepositDTO.getStatus());
                serveDepositDTO.setVehicleNum("");
                serveDepositDTO.setDeliverVehicleDate("");
                serveDepositDTO.setRecoverVehicleDate("");
            } else {
                serveDepositDTO.setVehicleNum(deliverDTO.getCarNum());
                DeliverVehicleDTO deliverVehicleDTO = deliverVehicleMap.get(deliverDTO.getDeliverNo());
                RecoverVehicleDTO recoverVehicleDTO = recoverVehicleMap.get(deliverDTO.getDeliverNo());
                supplyDepositData(serveDepositDTO, deliverDTO, deliverVehicleDTO, recoverVehicleDTO);
            }
            serveDepositDTO.setRecoverFeeConfirmFlag(!CollectionUtil.isNotEmpty(deliverNotCompletedMap.get(deliverDTO.getDeliverNo())));

        }
        return depositPagePagination;
    }

    private PagePagination<ServeDepositDTO> getServeDepositByCarId(CustomerDepositListDTO customerDepositLisDTO) {
        List<ServeDepositDTO> depositDTOList = new ArrayList<>();
        PageInfo<ServeDepositDTO> pageInfo = new PageInfo<>();
        PagePagination<ServeDepositDTO> pagePagination = new PagePagination<>();
        //存在车牌号查询 先查询交付单
        pageInfo.setPrePage(1);
        pageInfo.setPages(1);
        pageInfo.setPageSize(1);
        pageInfo.setTotal(0);
        pageInfo.setPageNum(0);
        pageInfo.setTotal(0);
        DeliverDTO deliverDTO = deliverEntityApi.getDeliverDTOByCarId(customerDepositLisDTO.getCarId());
        if (Objects.isNull(deliverDTO)) {
            log.warn("交付单不存在，车辆id：{}", customerDepositLisDTO.getCarId());
            pagePagination.setPageInfo(pageInfo);
            pagePagination.setList(depositDTOList);
            return pagePagination;
        }
        customerDepositLisDTO.setServeNo(deliverDTO.getServeNo());
        //在跟据服务单编号以及状态查询服务单
        ServeDepositDTO serveDepositDTO = serveEntityApi.getServeDepositByServeNo(customerDepositLisDTO);

        if (Objects.isNull(serveDepositDTO)) {
            log.error("服务单不存在，服务单编号：{}", deliverDTO.getServeNo());
            pagePagination.setPageInfo(pageInfo);
            pagePagination.setList(depositDTOList);
            return pagePagination;
        }
        serveDepositDTO.setStatusDisplay(ServeEnum.getServeEnum(serveDepositDTO.getStatus()).getStatus());

        if (deliverDTO.getDeliverStatus().equals(DeliverEnum.COMPLETED.getCode())) {
            serveDepositDTO.setRecoverFeeConfirmFlag(true);
        } else {
            serveDepositDTO.setRecoverFeeConfirmFlag(false);
        }
        serveDepositDTO.setVehicleNum(deliverDTO.getCarNum());
        DeliverVehicleDTO deliverVehicleDTO = deliverVehicleEntityApi.getDeliverVehicleByDeliverNo(deliverDTO.getDeliverNo());
        RecoverVehicleDTO recoverVehicleDTO = recoverVehicleEntityApi.getRecoverVehicleByDeliverNo(deliverDTO.getDeliverNo());

        supplyDepositData(serveDepositDTO, deliverDTO, deliverVehicleDTO, recoverVehicleDTO);
        depositDTOList.add(serveDepositDTO);

        pageInfo.setPrePage(1);
        pageInfo.setPages(1);
        pageInfo.setPageSize(1);
        pageInfo.setTotal(1);
        pageInfo.setPageNum(1);
        pageInfo.setTotal(1);
        pagePagination.setPageInfo(pageInfo);
        pagePagination.setList(depositDTOList);
        return pagePagination;
    }

    private void supplyDepositData(ServeDepositDTO serveDepositDTO, DeliverDTO deliverDTO, DeliverVehicleDTO deliverVehicleDTO, RecoverVehicleDTO recoverVehicleDTO) {
        if (Objects.isNull(deliverVehicleDTO)) {
            log.warn("发车单不存在,交付单编号：{}，交付单状态：{}", deliverDTO.getDeliverNo(), deliverDTO.getStatus());
            serveDepositDTO.setDeliverVehicleDate("");
        } else {
            serveDepositDTO.setDeliverVehicleDate(DateUtil.formatDate(deliverVehicleDTO.getDeliverVehicleTime()));
        }
        if (Objects.isNull(recoverVehicleDTO) || Objects.isNull(recoverVehicleDTO.getRecoverVehicleTime())) {
            log.warn("收车单不存在,交付单编号：{}，交付单状态：{}", deliverDTO.getDeliverNo(), deliverDTO.getStatus());
            serveDepositDTO.setRecoverVehicleDate("");
        } else {
            serveDepositDTO.setRecoverVehicleDate(DateUtil.formatDate(recoverVehicleDTO.getRecoverVehicleTime()));
        }

    }

}
