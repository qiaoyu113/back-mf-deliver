package com.mfexpress.rent.deliver.delivervehicle.executor;

import javax.annotation.Resource;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.billing.pay.api.app.AdvancePaymentAggregateRootApi;
import com.mfexpress.billing.pay.constant.PrepaymentServeMappingStatusEnum;
import com.mfexpress.billing.pay.dto.data.PrepaymentServeMappingDTO;
import com.mfexpress.billing.pay.dto.qry.PrepaymentServeMappingQry;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.component.starter.tools.mq.MqTools;
import com.mfexpress.component.utils.util.DateUtils;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.config.DeliverProjectProperties;
import com.mfexpress.rent.deliver.constant.DeliverContractStatusEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.LeaseModelEnum;
import com.mfexpress.rent.deliver.domainapi.DailyAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.daily.CreateDailyCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleImgCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCheckCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd.DeliverVehicleProcessCmdExe;
import com.mfexpress.rent.deliver.serve.executor.ReactiveServeCheckCmdExe;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.PolicyStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidStockStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleDto;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInsuranceDTO;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DeliverVehicleExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private AdvancePaymentAggregateRootApi advancePaymentAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource
    private DailyAggregateRootApi dailyAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private ReactiveServeCheckCmdExe reactiveServeCheck;

    @Resource(name = "serveSyncServiceImpl")
    private EsSyncHandlerI syncServiceI;

    @Resource
    private MqTools mqTools;

    @Value("${rocketmq.listenEventTopic}")
    private String event;

    public Integer execute(DeliverVehicleCmd cmd, TokenInfo tokenInfo) {
        // 发车日期校验
        checkDeliverVehicleTime(cmd);

        // 数据准备
        List<String> serveNos = cmd.getDeliverVehicleImgCmdList().stream().map(DeliverVehicleImgCmd::getServeNo).collect(Collectors.toList());
        Result<List<ServeDTO>> serveDTOSResult = serveAggregateRootApi.getServeDTOByServeNoList(serveNos);
        List<ServeDTO> serveDTOS = ResultDataUtils.getInstance(serveDTOSResult).getDataOrNull();
        if (CollectionUtils.isEmpty(serveDTOS) || serveDTOS.size() != serveNos.size()) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "服务单信息查询失败");
        }
        Map<String, ServeDTO> serveDTOMap = serveDTOS.stream().collect(Collectors.toMap(ServeDTO::getServeNo, Function.identity(), (v1, v2) -> v1));

        // 保险状态校验
        checkVehicleInsurance(cmd, serveDTOMap);

        // 重新激活服务单校验
        ReactivateServeCheckCmd reactivateServeCheckCmd = ReactivateServeCheckCmd.builder().serveNoList(serveNos)
                .deliverVehicleTime(cmd.getDeliverVehicleTime())
                .build();
        reactiveServeCheck.execute(reactivateServeCheckCmd);

        Result<List<DeliverDTO>> deliverDTOSResult = deliverAggregateRootApi.getDeliverDTOListByServeNoList(serveNos);
        List<DeliverDTO> deliverDTOS = ResultDataUtils.getInstance(deliverDTOSResult).getDataOrNull();
        if (CollectionUtils.isEmpty(deliverDTOS) || deliverDTOS.size() != serveNos.size()) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "交付单信息查询失败");
        }

        // 交付单状态检查，交车电子合同签署状态应为0未签署
        deliverCheck(deliverDTOS);

        PrepaymentServeMappingQry prepaymentServeMappingQry = new PrepaymentServeMappingQry();
        prepaymentServeMappingQry.setServeNos(serveNos);
        Result<List<PrepaymentServeMappingDTO>> prepaymentServeMappingDTOSResult = advancePaymentAggregateRootApi.getPrepaymentServeMappingDTOS(prepaymentServeMappingQry);
        List<PrepaymentServeMappingDTO> prepaymentServeMappingDTOS = Optional.ofNullable(ResultDataUtils.getInstance(prepaymentServeMappingDTOSResult).getDataOrNull()).orElse(new ArrayList<>());
        Map<String, PrepaymentServeMappingDTO> prepaymentServeMappingDTOMap = prepaymentServeMappingDTOS.stream().filter(p -> p.getStatus().equals(PrepaymentServeMappingStatusEnum.INIT.getCode())).collect(Collectors.toMap(PrepaymentServeMappingDTO::getServeNo, a -> a));

        // 计算预计收车日期
        Map<String, String> expectRecoverDateMap = new HashMap<>(serveDTOS.size());
        for (String serveNo : serveNos) {
            ServeDTO serve = serveDTOMap.get(serveNo);
            //替换车使用维修车的预计收车日期，重新激活的服务单不更新预计收车日期
            if (!JudgeEnum.YES.getCode().equals(serve.getReplaceFlag()) && !JudgeEnum.YES.getCode().equals(serve.getReactiveFlag())) {
                String expectRecoverDate = DeliverVehicleProcessCmdExe.getExpectRecoverDate(cmd.getDeliverVehicleTime(), serve.getLeaseMonths(), serve.getLeaseDays());
                expectRecoverDateMap.put(serveNo, expectRecoverDate);
            }
        }

        // 更新服务单、交付单，创建交车单
        cmd.setOperatorId(tokenInfo.getId());
        cmd.setOperatorName(tokenInfo.getNickName());
        cmd.setExpectRecoverDateMap(expectRecoverDateMap);
        Result<Integer> deliverResult = serveAggregateRootApi.deliverVehicles(cmd);
        ResultValidUtils.checkResultException(deliverResult);

        // 发送计费消息
        List<Integer> carIdList = new LinkedList<>();
        cmd.getDeliverVehicleImgCmdList().forEach(deliverImgInfo -> {
            carIdList.add(deliverImgInfo.getCarId());
            //发车操作mq触发计费
            ServeDTO serve = serveDTOMap.get(deliverImgInfo.getServeNo());
            com.mfexpress.billing.rentcharge.dto.data.deliver.DeliverVehicleCmd rentChargeCmd = new com.mfexpress.billing.rentcharge.dto.data.deliver.DeliverVehicleCmd();
            rentChargeCmd.setServeNo(deliverImgInfo.getServeNo());
            rentChargeCmd.setDeliverNo(deliverImgInfo.getDeliverNo());
            rentChargeCmd.setRent(serve.getRent());
            String expectRecoverDate = expectRecoverDateMap.get(deliverImgInfo.getServeNo());
            if (Objects.isNull(expectRecoverDate)) {
                //替换车使用原车的预计收车日期作为计费截止日期，重新激活服务单使用原来的预计收车日期作为计费截止日期
                rentChargeCmd.setExpectRecoverDate(serve.getExpectRecoverDate());
            } else {
                rentChargeCmd.setExpectRecoverDate(expectRecoverDate);
            }
            rentChargeCmd.setDeliverFlag(true);
            rentChargeCmd.setCustomerId(serve.getCustomerId());
            rentChargeCmd.setCreateId(cmd.getOperatorId());
            rentChargeCmd.setVehicleId(deliverImgInfo.getCarId());
            rentChargeCmd.setDeliverDate(DateUtil.formatDate(cmd.getDeliverVehicleTime()));
            rentChargeCmd.setRentRatio(serve.getRentRatio().doubleValue());
            rentChargeCmd.setBusinessType(serve.getBusinessType());

            PrepaymentServeMappingDTO prepaymentServeMappingDTO = prepaymentServeMappingDTOMap.get(serve.getServeNo());
            if (Objects.isNull(prepaymentServeMappingDTO)) {
                rentChargeCmd.setAdvancePaymentAmount(BigDecimal.ZERO);
            } else {
                rentChargeCmd.setAdvancePaymentAmount(prepaymentServeMappingDTO.getPrepaymentAmount());
            }
            mqTools.send(event, "deliver_vehicle", null, JSON.toJSONString(rentChargeCmd));
        });

        // 修改对应的车辆状态为租赁状态
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.OUT.getCode());
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.LEASE.getCode());
        vehicleSaveCmd.setId(carIdList);
        vehicleSaveCmd.setCustomerId(cmd.getCustomerId());
        //增加通知车辆域业务类型以及收发车类型
        //相同订单业务类型相同
        vehicleSaveCmd.setBuType(serveDTOS.get(0).getBusinessType());
        //1发车 2收车
        vehicleSaveCmd.setRecoverDeliverFlag(1);
        Result<CustomerVO> customerResult = customerAggregateRootApi.getById(cmd.getCustomerId());
        if (customerResult.getData() != null) {
            vehicleSaveCmd.setAddress(customerResult.getData().getName());
        }
        Result<String> vehicleResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if (ResultErrorEnum.SUCCESSED.getCode() != vehicleResult.getCode()) {
            log.error("发车电子合同签署完成时，更改车辆状态失败。车辆id：{}", carIdList);
        }

        //生成日报
        CreateDailyCmd createDailyCmd = new CreateDailyCmd();
        createDailyCmd.setDeliverFlag(1);
        createDailyCmd.setDeliverRecoverDate(cmd.getDeliverVehicleTime());
        createDailyCmd.setServeNoList(serveNos);
        dailyAggregateRootApi.createDaily(createDailyCmd);

        //强制同步
        Map<String, String> map = new HashMap<>();
        serveNos.forEach(s -> {
            map.put("serve_no", s);
            syncServiceI.execOne(map);
        });

        return 0;
    }

    private void deliverCheck(List<DeliverDTO> deliverDTOS) {
        deliverDTOS.forEach(deliver -> {
            if (DeliverContractStatusEnum.NOSIGN.getCode() != deliver.getDeliverContractStatus()) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "选中车辆存在电子交接单，请回列表页查看");
            }
        });
    }

    private void checkVehicleInsurance(DeliverVehicleCmd cmd, Map<String, ServeDTO> serveMap) {
        List<DeliverVehicleImgCmd> deliverVehicleInfos = cmd.getDeliverVehicleImgCmdList();
        List<Integer> vehicleIds = deliverVehicleInfos.stream().map(DeliverVehicleImgCmd::getCarId).collect(Collectors.toList());
        Result<List<VehicleDto>> vehicleDTOSResult = vehicleAggregateRootApi.getVehicleDTOByIds(vehicleIds);
        List<VehicleDto> vehicleDTOS = ResultDataUtils.getInstance(vehicleDTOSResult).getDataOrException();
        if (null == vehicleDTOS || vehicleDTOS.isEmpty()) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆信息查询失败");
        }
        Map<Integer, VehicleDto> vehicleMap = vehicleDTOS.stream().collect(Collectors.toMap(VehicleDto::getVehicleId, Function.identity(), (v1, v2) -> v1));
        Result<List<VehicleInsuranceDTO>> vehicleInsuranceDTOSResult = vehicleAggregateRootApi.getVehicleInsuranceByVehicleIds(vehicleIds);
        List<VehicleInsuranceDTO> vehicleInsuranceDTOS = ResultDataUtils.getInstance(vehicleInsuranceDTOSResult).getDataOrException();
        if (null == vehicleInsuranceDTOS || vehicleInsuranceDTOS.isEmpty() || vehicleIds.size() != vehicleInsuranceDTOS.size()) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆保险信息查询失败");
        }
        Map<Integer, VehicleInsuranceDTO> insuranceDTOMap = vehicleInsuranceDTOS.stream().collect(Collectors.toMap(VehicleInsuranceDTO::getVehicleId, Function.identity(), (v1, v2) -> v1));

        deliverVehicleInfos.forEach(deliverVehicleInfo -> {
            VehicleInsuranceDTO vehicleInsuranceDTO = insuranceDTOMap.get(deliverVehicleInfo.getCarId());
            if (null == vehicleInsuranceDTO) {
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "车辆保险信息查询失败");
            }
            VehicleDto vehicleDTO = vehicleMap.get(deliverVehicleInfo.getCarId());
            if (PolicyStatusEnum.EXPIRED.getCode() == vehicleInsuranceDTO.getCompulsoryInsuranceStatus()) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "车辆".concat(vehicleDTO == null ? "" : vehicleDTO.getPlateNumber()).concat("的交强险不在在保状态，请重新确认"));
            }
            ServeDTO serve = serveMap.get(deliverVehicleInfo.getServeNo());
            if (null == serve) {
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "服务单查询失败");
            }
            if (LeaseModelEnum.SHOW.getCode() != serve.getLeaseModelId()) {
                if (PolicyStatusEnum.EXPIRED.getCode() == vehicleInsuranceDTO.getCommercialInsuranceStatus()) {
                    throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "车辆".concat(vehicleDTO == null ? "" : vehicleDTO.getPlateNumber()).concat("的商业险不在在保状态，请重新确认"));
                }
            }
        });
    }

    private void checkDeliverVehicleTime(DeliverVehicleCmd cmd) {
        Date deliverVehicleTime = cmd.getDeliverVehicleTime();
        Date tSubNDate = DateUtils.addDate(DateUtil.parseDate(DateUtil.now()), -DeliverProjectProperties.DELIVER_TIME_RANGE.getPre());
        Date tAddNDate = DateUtils.addDate(DateUtil.parseDate(DateUtil.now()), DeliverProjectProperties.DELIVER_TIME_RANGE.getSuf());
        if (deliverVehicleTime.before(tSubNDate) ||
                deliverVehicleTime.after(tAddNDate)) {
            throw new CommonException(ResultErrorEnum.VILAD_ERROR.getCode(), "收车日期超出可选范围");
        }

        List<Integer> vehicleId = cmd.getDeliverVehicleImgCmdList().stream().map(DeliverVehicleImgCmd::getCarId).distinct().collect(Collectors.toList());
        if (CollectionUtil.isEmpty(vehicleId)) {
            log.error("判断车辆最晚收车日期出错 未查询到车辆Id  参数:{}", cmd);
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "未查询到车辆Id");
        }

        Result<List<DeliverDTO>> deliverDTOSResult = deliverAggregateRootApi.getDeliverDTOSByCarIdList(vehicleId);
        log.info("发车验车 查询deliver 参数:{},结果:{}",vehicleId,deliverDTOSResult);
        if (CollectionUtil.isNotEmpty(deliverDTOSResult.getData())) {
            List<String> deliverNo = deliverDTOSResult.getData().stream().map(DeliverDTO::getDeliverNo).distinct().collect(Collectors.toList());
            Result<List<RecoverVehicleDTO>> recoverVehicleDTOSResult = recoverVehicleAggregateRootApi.getRecoverVehicleDTOByDeliverNos(deliverNo);
            log.info("发车验车 查询发车单 参数:{},结果:{}",deliverNo,recoverVehicleDTOSResult);
            if (CollectionUtil.isNotEmpty(recoverVehicleDTOSResult.getData())) {
                if (cmd.getDeliverVehicleTime().before(recoverVehicleDTOSResult.getData().get(0).getRecoverVehicleTime())) {
                    log.error("发车日小于上次租赁收车日期  参数:{}", cmd);
                    throw new CommonException(ResultErrorEnum.VILAD_ERROR.getCode(), "发车日小于上次租赁收车日期");
                }
            }
        }
    }

}

