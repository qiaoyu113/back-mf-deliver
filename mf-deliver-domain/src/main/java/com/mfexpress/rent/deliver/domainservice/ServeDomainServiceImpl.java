package com.mfexpress.rent.deliver.domainservice;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageInfo;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleImgCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositListDTO;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositLockListDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDepositDTO;
import com.mfexpress.rent.deliver.dto.data.serve.dto.ContractWillExpireInfoDTO;
import com.mfexpress.rent.deliver.dto.data.serve.qry.ContractWillExpireQry;
import com.mfexpress.rent.deliver.entity.*;
import com.mfexpress.rent.deliver.entity.api.DeliverEntityApi;
import com.mfexpress.rent.deliver.entity.api.DeliverVehicleEntityApi;
import com.mfexpress.rent.deliver.entity.api.RecoverVehicleEntityApi;
import com.mfexpress.rent.deliver.entity.api.ServeEntityApi;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
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

    @Resource
    private RedisTools redisTools;

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
                DeliverVehicleDTO deliverVehicleDTO = deliverVehicleMap.get(deliverDTO.getDeliverNo());
                if (Objects.isNull(deliverVehicleDTO)) {
                    lockListDTO.setDeliverVehicleDate("");
                } else {
                    lockListDTO.setDeliverVehicleDate(DateUtil.formatDate(deliverVehicleDTO.getDeliverVehicleTime()));
                }
            }
            depositLockListDTOS.add(lockListDTO);

        }
        return depositLockListDTOS;

    }

    @Override
    public List<ContractWillExpireInfoDTO> getContractThatWillExpire(ContractWillExpireQry contractWillExpireQry) {
        //查询指定状态状态,下指定时间收车的服务单
        List<ServeDTO> serveDTOList = serveEntityApi.getWillRecoverService(contractWillExpireQry);
        if (CollUtil.isEmpty(serveDTOList)) {
            return new ArrayList<>();
        }
        Map<String, ServeDTO> serveMap = serveDTOList.stream().collect(Collectors.toMap(ServeDTO::getServeNo, Function.identity(), (k1, k2) -> k1));
        //状态有效的交付单
        List<DeliverDTO> deliverDTOList = deliverEntityApi.getDeliverDTOListByServeNoList(new ArrayList<>(serveMap.keySet()));
        Map<String, DeliverDTO> deliverMap = deliverDTOList.stream().collect(Collectors.toMap(DeliverDTO::getServeNo, Function.identity(), (k1, k2) -> k1));

        return serveDTOList.stream().map(serveDto -> {
            String serveNo = serveDto.getServeNo();
            ContractWillExpireInfoDTO contractWillExpireInfoDTO = new ContractWillExpireInfoDTO();
            //orderId;serveId; serveNo;customerId;status; oaContractCode;expectRecoverDate;orgId;
            BeanUtil.copyProperties(serveDto, contractWillExpireInfoDTO, CopyOptions.create().ignoreError());
            //;deliverNo;;carId;;carNum;

            if (CollUtil.isNotEmpty(deliverMap) && deliverMap.get(serveNo) != null) {
                DeliverDTO deliverDTO = deliverMap.get(serveNo);
                contractWillExpireInfoDTO.setCarId(deliverDTO.getCarId());
                contractWillExpireInfoDTO.setCarNum(deliverDTO.getCarNum());
                contractWillExpireInfoDTO.setDeliverNo(deliverDTO.getDeliverNo());
            }

            return contractWillExpireInfoDTO;
        }).collect(Collectors.toList());
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
            if (serveDepositDTO.getStatus() < ServeEnum.RECOVER.getCode() || serveDepositDTO.getStatus().equals(ServeEnum.REPAIR.getCode())) {
                serveDepositDTO.setRecoverFeeConfirmFlag(null);
            } else {
                serveDepositDTO.setRecoverFeeConfirmFlag(CollectionUtil.isEmpty(deliverNotCompletedMap.get(serveDepositDTO.getServeNo())));
            }


        }
        return depositPagePagination;
    }

    private PagePagination<ServeDepositDTO> getServeDepositByCarId(CustomerDepositListDTO customerDepositLisDTO) {
        List<ServeDepositDTO> depositDTOList = new ArrayList<>();
        PageInfo<ServeDepositDTO> pageInfo = new PageInfo<>();

        //存在车牌号查询 先查询交付单
        pageInfo.setPrePage(0);
        pageInfo.setPages(0);
        pageInfo.setPageSize(0);
        pageInfo.setTotal(0);
        pageInfo.setPageNum(1);
        pageInfo.setTotal(0);
        DeliverDTO deliverDTO = deliverEntityApi.getDeliverDTOByCarId(customerDepositLisDTO.getCarId());
        if (Objects.isNull(deliverDTO)) {
            log.warn("交付单不存在，车辆id：{}", customerDepositLisDTO.getCarId());
            PagePagination<ServeDepositDTO> pagePagination = new PagePagination<>(pageInfo);
            pagePagination.setList(depositDTOList);
            return pagePagination;
        }
        customerDepositLisDTO.setServeNo(deliverDTO.getServeNo());
        //在跟据服务单编号以及状态查询服务单
        ServeDepositDTO serveDepositDTO = serveEntityApi.getServeDepositByServeNo(customerDepositLisDTO);

        if (Objects.isNull(serveDepositDTO)) {
            log.error("服务单不存在，服务单编号：{}", deliverDTO.getServeNo());
            PagePagination<ServeDepositDTO> pagePagination = new PagePagination<>(pageInfo);
            pagePagination.setList(depositDTOList);
            return pagePagination;
        }

        serveDepositDTO.setStatusDisplay(ServeEnum.getServeEnum(serveDepositDTO.getStatus()).getStatus());
        List<DeliverDTO> deliverNotCompleteList = deliverEntityApi.getDeliverNotComplete(Collections.singletonList(deliverDTO.getServeNo()));
        //收车费用状态
        if (serveDepositDTO.getStatus() < ServeEnum.RECOVER.getCode() || serveDepositDTO.getStatus().equals(ServeEnum.REPAIR.getCode())) {
            serveDepositDTO.setRecoverFeeConfirmFlag(null);
        } else {
            serveDepositDTO.setRecoverFeeConfirmFlag(CollectionUtil.isEmpty(deliverNotCompleteList));
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
        PagePagination<ServeDepositDTO> pagePagination = new PagePagination<>(pageInfo);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deliverVehicles(DeliverVehicleCmd cmd) {
        List<DeliverVehicleImgCmd> deliverInfos = cmd.getDeliverVehicleImgCmdList();
        List<String> serveNoList = new LinkedList<>();
        List<DeliverVehicleDTO> deliverVehicleDTOS = new LinkedList<>();
        List<DeliverMethodPO> deliverMethodPOS = new LinkedList<>();
        deliverInfos.forEach(deliverInfo -> {
            DeliverVehicleDTO deliverVehicleDTO = new DeliverVehicleDTO();
            deliverVehicleDTO.setServeNo(deliverInfo.getServeNo());
            deliverVehicleDTO.setDeliverNo(deliverInfo.getDeliverNo());
            deliverVehicleDTO.setImgUrl(deliverInfo.getImgUrl());
            deliverVehicleDTO.setContactsName(cmd.getContactsName());
            deliverVehicleDTO.setContactsPhone(cmd.getContactsPhone());
            deliverVehicleDTO.setContactsCard(cmd.getContactsCard());
            deliverVehicleDTO.setDeliverVehicleTime(cmd.getDeliverVehicleTime());
            deliverVehicleDTOS.add(deliverVehicleDTO);
            serveNoList.add(deliverInfo.getServeNo());
        });

        // 服务单状态更新为已发车 填充预计收车日期
        Map<String, String> expectRecoverDateMap = cmd.getExpectRecoverDateMap();
        for (String serveNo : serveNoList) {
            ServeEntity serve = ServeEntity.builder().status(ServeEnum.DELIVER.getCode()).build();
            String expectRecoverDate = expectRecoverDateMap.get(serveNo);
            if (Objects.nonNull(expectRecoverDate)) {
                serve.setExpectRecoverDate(expectRecoverDate);
            }
            serve.setUpdateId(cmd.getOperatorId());
            serveEntityApi.updateServeByServeNo(serveNo, serve);
        }

        // 交付单状态更新为已发车并初始化操作状态
        DeliverEntity deliver = DeliverEntity.builder()
                .deliverStatus(DeliverEnum.DELIVER.getCode())
                .isCheck(JudgeEnum.NO.getCode())
                .isInsurance(JudgeEnum.NO.getCode())
                .updateId(cmd.getOperatorId())
                .build();
        deliverEntityApi.updateDeliverByServeNoList(serveNoList, deliver);

        // 生成发车单
        List<DeliverVehicleEntity> deliverVehicleList = new ArrayList<>();
        deliverVehicleDTOS.forEach(deliverVehicleDTO -> {
            long incr = redisTools.incr(DeliverUtils.getEnvVariable(Constants.REDIS_DELIVER_VEHICLE_KEY) + DeliverUtils.getDateByYYMMDD(new Date()), 1);
            String deliverVehicleNo = DeliverUtils.getNo(Constants.REDIS_DELIVER_VEHICLE_KEY, incr);
            DeliverVehicleEntity deliverVehicle = new DeliverVehicleEntity();
            BeanUtils.copyProperties(deliverVehicleDTO, deliverVehicle);
            deliverVehicle.setDeliverVehicleNo(deliverVehicleNo);
            deliverVehicleList.add(deliverVehicle);

            DeliverMethodPO deliverMethodPO = new DeliverMethodPO();
            deliverMethodPO.setDeliverType(DeliverTypeEnum.DELIVER.getCode());
            deliverMethodPO.setDeliverNo(deliverVehicleDTO.getDeliverNo());
            deliverMethodPO.setDeliverRecoverVehicleNo(deliverVehicleNo);
            deliverMethodPO.setDeliverMethod(cmd.getDeliverMethod());
            deliverMethodPO.setHandoverImgUrls(JSONUtil.toJsonStr(cmd.getHandoverImgUrls()));
            deliverMethodPO.setCreatorId(cmd.getOperatorId());
            deliverMethodPO.setUpdaterId(cmd.getOperatorId());
            deliverMethodPOS.add(deliverMethodPO);
        });
        deliverVehicleEntityApi.addDeliverVehicle(deliverVehicleList);

        // 保存发车方式
        deliverVehicleEntityApi.saveDeliverMethods(deliverMethodPOS);

        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer recoverVehicle(RecoverVehicleCmd cmd) {
        // 服务单变为已收车
        ServeEntity serve = ServeEntity.builder().status(ServeEnum.RECOVER.getCode()).updateId(cmd.getOperatorId()).build();
        serveEntityApi.updateServeByServeNo(cmd.getServeNo(), serve);

        // 交付单变为已收车
        DeliverEntity deliverToUpdate = new DeliverEntity();
        deliverToUpdate.setDeliverStatus(DeliverEnum.RECOVER.getCode());
        deliverToUpdate.setUpdateId(cmd.getOperatorId());
        deliverEntityApi.updateDeliverByServeNoList(Collections.singletonList(cmd.getServeNo()), deliverToUpdate);

        // 修改收车单
        RecoverVehicleDTO recoverVehicleDTO = recoverVehicleEntityApi.getRecoverVehicleByDeliverNo(cmd.getDeliverNo());
        if (null == recoverVehicleDTO) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "收车单查询失败");
        }
        RecoverVehicleEntity recoverVehicleToUpdate = new RecoverVehicleEntity();
        recoverVehicleToUpdate.setDeliverNo(cmd.getDeliverNo());
        recoverVehicleToUpdate.setContactsName(cmd.getContactsName());
        recoverVehicleToUpdate.setContactsCard(cmd.getContactsCard());
        recoverVehicleToUpdate.setContactsPhone(cmd.getContactsPhone());
        recoverVehicleToUpdate.setRecoverVehicleTime(cmd.getRecoverVehicleTime());
        recoverVehicleToUpdate.setDamageFee(cmd.getDamageFee());
        recoverVehicleToUpdate.setParkFee(cmd.getParkFee());
        recoverVehicleToUpdate.setWareHouseId(cmd.getWareHouseId());
        recoverVehicleToUpdate.setImgUrl(cmd.getImgUrl());
        recoverVehicleToUpdate.setUpdateId(cmd.getOperatorId());
        recoverVehicleEntityApi.updateRecoverVehicleByDeliverNo(recoverVehicleToUpdate);

        // 保存收车方式
        DeliverMethodPO deliverMethodPO = new DeliverMethodPO();
        deliverMethodPO.setDeliverType(DeliverTypeEnum.RECOVER.getCode());
        deliverMethodPO.setDeliverNo(cmd.getDeliverNo());
        deliverMethodPO.setDeliverRecoverVehicleNo(recoverVehicleDTO.getRecoverVehicleNo());
        deliverMethodPO.setDeliverMethod(cmd.getDeliverMethod());
        deliverMethodPO.setHandoverImgUrls(JSONUtil.toJsonStr(cmd.getHandoverImgUrls()));
        deliverMethodPO.setCreatorId(cmd.getOperatorId());
        deliverMethodPO.setUpdaterId(cmd.getOperatorId());
        deliverVehicleEntityApi.saveDeliverMethods(Collections.singletonList(deliverMethodPO));

        return 0;
    }

}
