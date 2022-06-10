package com.mfexpress.rent.deliver.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.billing.rentcharge.dto.data.deliver.DeliverVehicleCmd;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.mq.MqTools;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.DeliverContractStatusEnum;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.DailyAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.daily.CreateDailyCmd;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.cmd.DeliverVehicleProcessCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverImgInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.entity.DeliverVehicle;
import com.mfexpress.rent.deliver.entity.DeliverEntity;
import com.mfexpress.rent.deliver.entity.DeliverVehicleEntity;
import com.mfexpress.rent.deliver.entity.ServeEntity;
import com.mfexpress.rent.deliver.entity.api.DeliverVehicleEntityApi;
import com.mfexpress.rent.deliver.gateway.DeliverGateway;
import com.mfexpress.rent.deliver.gateway.DeliverVehicleGateway;
import com.mfexpress.rent.deliver.gateway.ServeGateway;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidStockStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/domain/deliver/v3/delivervehcile")
@Api(tags = "domain--交付--1.4发车单聚合")
public class DeliverVehicleAggregateRootApiImpl implements DeliverVehicleAggregateRootApi {

    @Resource
    private RedisTools redisTools;
    @Resource
    private DeliverVehicleGateway deliverVehicleGateway;

    @Resource
    private ServeGateway serveGateway;

    @Resource
    private DeliverGateway deliverGateway;
    @Resource
    private DeliverVehicleEntityApi deliverVehicleEntityApi;

    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource
    private DailyAggregateRootApi dailyAggregateRootApi;

    @Resource
    private ElecHandoverContractAggregateRootApi elecHandoverContractAggregateRootApi;

    @Resource
    private MqTools mqTools;

    @Value("${rocketmq.listenEventTopic}")
    private String event;

    @Override
    @PostMapping("/getDeliverVehicleDto")
    @PrintParam
    public Result<DeliverVehicleDTO> getDeliverVehicleDto(@RequestParam("deliverNo") String deliverNo) {
        DeliverVehicleEntity deliverVehicle = deliverVehicleGateway.getDeliverVehicleByDeliverNo(deliverNo);
        DeliverVehicleDTO deliverVehicleDTO = new DeliverVehicleDTO();
        if (deliverVehicle != null) {
            BeanUtils.copyProperties(deliverVehicle, deliverVehicleDTO);
            return Result.getInstance(deliverVehicleDTO).success();
        }
        return Result.getInstance(deliverVehicleDTO).success();
    }

    @Override
    @PostMapping("/addDeliverVehicle")
    @PrintParam
    public Result<String> addDeliverVehicle(@RequestBody List<DeliverVehicleDTO> deliverVehicleDTOList) {

        if (deliverVehicleDTOList != null) {
            List<DeliverVehicleEntity> deliverVehicleList = deliverVehicleDTOList.stream().map(deliverVehicleDTO -> {
                long incr = redisTools.incr(DeliverUtils.getEnvVariable(Constants.REDIS_DELIVER_VEHICLE_KEY) + DeliverUtils.getDateByYYMMDD(new Date()), 1);
                String deliverVehicleNo = DeliverUtils.getNo(Constants.REDIS_DELIVER_VEHICLE_KEY, incr);
                DeliverVehicleEntity deliverVehicle = new DeliverVehicleEntity();
                BeanUtils.copyProperties(deliverVehicleDTO, deliverVehicle);
                deliverVehicle.setDeliverVehicleNo(deliverVehicleNo);
                return deliverVehicle;
            }).collect(Collectors.toList());
            int i = deliverVehicleGateway.addDeliverVehicle(deliverVehicleList);
            return i > 0 ? Result.getInstance("发车成功").success() : Result.getInstance("发车成功").fail(-1, "发车失败");
        }
        return Result.getInstance("发车信息为空").fail(-1, "发车信息为空");
    }

    @Override
    @PostMapping("getDeliverVehicleByServeNo")
    @PrintParam
    public Result<Map<String, DeliverVehicle>> getDeliverVehicleByServeNo(@RequestBody List<String> serveNoList) {
        List<DeliverVehicleEntity> deliverVehicleList = deliverVehicleGateway.getDeliverVehicleByServeNo(serveNoList);
        Map<String, DeliverVehicle> deliverVehicleMap = new HashMap<>();
        deliverVehicleList.forEach(deliverVehicleEntity -> {
            DeliverVehicle deliverVehicle = BeanUtil.copyProperties(deliverVehicleEntity, DeliverVehicle.class);
            deliverVehicleMap.put(deliverVehicleEntity.getServeNo(), deliverVehicle);
        });
        return Result.getInstance(deliverVehicleMap).success();
    }

    @Override
    @PostMapping("getDeliverVehicleOneByServeNo")
    @PrintParam
    public Result< DeliverVehicleDTO> getDeliverVehicleOneByServeNo(@RequestBody String serveNo) {
        DeliverVehicleEntity deliverVehicleList = deliverVehicleGateway.getDeliverVehicleOneByServeNo(serveNo);


        DeliverVehicleDTO deliverVehicle = BeanUtil.copyProperties(deliverVehicleList, DeliverVehicleDTO.class);


        return Result.getInstance(deliverVehicle).success();
    }

    // 发车电子合同签署完成后触发的一系列操作
    @Override
    @PostMapping("/deliverVehicles")
    @PrintParam
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> deliverVehicles(@RequestBody ElecContractDTO contractDTO) {
        List<DeliverImgInfo> deliverImgInfos = JSONUtil.toList(contractDTO.getPlateNumberWithImgs(), DeliverImgInfo.class);
        if (deliverImgInfos.isEmpty()) {
            return Result.getInstance((Integer) null).fail(ResultErrorEnum.OPER_ERROR.getCode(), ResultErrorEnum.OPER_ERROR.getName());
        }
        List<String> serveNoList = new LinkedList<>();
        List<DeliverVehicleDTO> deliverVehicleDTOList = new LinkedList<>();
        for (DeliverImgInfo deliverImgInfo : deliverImgInfos) {
            serveNoList.add(deliverImgInfo.getServeNo());
            DeliverVehicleDTO deliverVehicleDTO = new DeliverVehicleDTO();
            deliverVehicleDTO.setServeNo(deliverImgInfo.getServeNo());
            deliverVehicleDTO.setDeliverNo(deliverImgInfo.getDeliverNo());
            deliverVehicleDTO.setImgUrl(deliverImgInfo.getImgUrl());
            deliverVehicleDTO.setContactsName(contractDTO.getContactsName());
            deliverVehicleDTO.setContactsPhone(contractDTO.getContactsPhone());
            deliverVehicleDTO.setContactsCard(contractDTO.getContactsCard());
            deliverVehicleDTO.setDeliverVehicleTime(contractDTO.getDeliverVehicleTime());
            deliverVehicleDTOList.add(deliverVehicleDTO);
        }

        // 服务单状态更新为已发车 填充预计收车日期
        Map<String, String> expectRecoverDateMap = contractDTO.getExpectRecoverDateMap();
        for (String serveNo : serveNoList) {
            ServeEntity serve = ServeEntity.builder().status(ServeEnum.DELIVER.getCode()).build();
            String expectRecoverDate = expectRecoverDateMap.get(serveNo);
            if (Objects.nonNull(expectRecoverDate)) {
                serve.setExpectRecoverDate(expectRecoverDate);
            }
            serveGateway.updateServeByServeNo(serveNo, serve);
        }


        // 交付单状态更新为已发车并初始化操作状态
        DeliverEntity deliver = DeliverEntity.builder()
                .deliverStatus(DeliverEnum.DELIVER.getCode())
                .isCheck(JudgeEnum.NO.getCode())
                .isInsurance(JudgeEnum.NO.getCode())
                .deliverContractStatus(DeliverContractStatusEnum.COMPLETED.getCode())
                .build();
        deliverGateway.updateDeliverByServeNoList(serveNoList, deliver);

        // 生成发车单
        List<DeliverVehicleEntity> deliverVehicleList = deliverVehicleDTOList.stream().map(deliverVehicleDTO -> {
            long incr = redisTools.incr(DeliverUtils.getEnvVariable(Constants.REDIS_DELIVER_VEHICLE_KEY) + DeliverUtils.getDateByYYMMDD(new Date()), 1);
            String deliverVehicleNo = DeliverUtils.getNo(Constants.REDIS_DELIVER_VEHICLE_KEY, incr);
            DeliverVehicleEntity deliverVehicle = new DeliverVehicleEntity();
            BeanUtils.copyProperties(deliverVehicleDTO, deliverVehicle);
            deliverVehicle.setDeliverVehicleNo(deliverVehicleNo);
            return deliverVehicle;
        }).collect(Collectors.toList());
        deliverVehicleGateway.addDeliverVehicle(deliverVehicleList);

        return Result.getInstance(0).success();
    }

    @Override
    @PostMapping("/getDeliverVehicleByDeliverNoList")
    @PrintParam
    public Result<List<DeliverVehicleDTO>> getDeliverVehicleByDeliverNoList(@RequestBody List<String> deliverNoList) {
        List<DeliverVehicleDTO> deliverVehicleDTOList = deliverVehicleEntityApi.getDeliverVehicleListByDeliverNoList(deliverNoList);
        if (CollectionUtil.isEmpty(deliverVehicleDTOList)) {
            return Result.getInstance(deliverVehicleDTOList).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
        return Result.getInstance(deliverVehicleDTOList).success();
    }

    @Override
    @PostMapping(value = "/deliver/process")
    @Transactional(rollbackFor = Exception.class)
    @PrintParam
    public Result<List<String>> deliverVehicleProcess(DeliverVehicleProcessCmd cmd) {

        ElecContractDTO contractDTO = cmd.getContractDTO();

        // 数据收集
        List<DeliverImgInfo> deliverImgInfos = JSONUtil.toList(contractDTO.getPlateNumberWithImgs(), DeliverImgInfo.class);
        List<String> serveNoList = deliverImgInfos.stream().map(DeliverImgInfo::getServeNo).collect(Collectors.toList());
        Result<List<ServeDTO>> serveDTOListResult = serveAggregateRootApi.getServeDTOByServeNoList(serveNoList);
        if (serveDTOListResult.getCode() != 0 || null == serveDTOListResult.getData() || serveDTOListResult.getData().isEmpty()) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "服务单信息不存在");
        }
        List<ServeDTO> serveDTOList = serveDTOListResult.getData();
        Map<String, ServeDTO> serveDTOMap = serveDTOList.stream().collect(Collectors.toMap(ServeDTO::getServeNo, Function.identity(), (v1, v2) -> v1));
        //每个服务单对应的预计收车日期
        Map<String, String> expectRecoverDateMap = new HashMap<>(serveDTOList.size());
        for (String serveNo : serveNoList) {
            ServeDTO serve = serveDTOMap.get(serveNo);
            //替换车使用维修车的预计收车日期，重新激活的服务单不更新预计收车日期
            if (!JudgeEnum.YES.getCode().equals(serve.getReplaceFlag()) && !JudgeEnum.YES.getCode().equals(serve.getReactiveFlag())) {
                String expectRecoverDate = getExpectRecoverDate(contractDTO.getDeliverVehicleTime(), serve.getLeaseMonths(), serve.getLeaseDays());
                expectRecoverDateMap.put(serveNo, expectRecoverDate);
            }
        }
        contractDTO.setExpectRecoverDateMap(expectRecoverDateMap);
        //生成发车单 交付单状态更新已发车并初始化操作状态  服务单状态更新为已发车
        Result<Integer> result = deliverVehicleAggregateRootApi.deliverVehicles(contractDTO);
        ResultValidUtils.checkResultException(result);
        List<Integer> carIdList = new LinkedList<>();
        deliverImgInfos.forEach(deliverImgInfo -> {
            carIdList.add(deliverImgInfo.getCarId());
            //发车操作mq触发计费
            ServeDTO serve = serveDTOMap.get(deliverImgInfo.getServeNo());
            DeliverVehicleCmd rentChargeCmd = new DeliverVehicleCmd();
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
            rentChargeCmd.setCreateId(contractDTO.getCreatorId());
            rentChargeCmd.setVehicleId(deliverImgInfo.getCarId());
            rentChargeCmd.setDeliverDate(DateUtil.formatDate(contractDTO.getDeliverVehicleTime()));
            mqTools.send(event, "deliver_vehicle", null, JSON.toJSONString(rentChargeCmd));
        });

        // 修改对应的车辆状态为租赁状态
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.OUT.getCode());
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.LEASE.getCode());
        vehicleSaveCmd.setId(carIdList);
        vehicleSaveCmd.setCustomerId(cmd.getCustomerId());
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
        createDailyCmd.setDeliverRecoverDate(contractDTO.getDeliverVehicleTime());
        createDailyCmd.setServeNoList(serveNoList);

        dailyAggregateRootApi.createDaily(createDailyCmd);

        return Result.getInstance(serveNoList).success();
    }

    private String getExpectRecoverDate(Date deliverVehicleDate, Integer offsetMonths, Integer offsetDays) {
        DateTime dateTime = DateUtil.endOfMonth(deliverVehicleDate);
        String deliverDate = DateUtil.formatDate(deliverVehicleDate);
        String endDate = DateUtil.formatDate(dateTime);
        if (deliverDate.equals(endDate)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateUtil.endOfMonth(dateTime));
            if (null != offsetMonths) {
                calendar.add(Calendar.MONTH, offsetMonths);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
            if (null != offsetDays) {
                // offsetDays -= 1;
                if (offsetDays > 0) {
                    calendar.add(Calendar.DAY_OF_MONTH, offsetDays);
                }
            }
            return DateUtil.formatDate(calendar.getTime());
        } else {
            if (null != offsetMonths) {
                deliverVehicleDate = DateUtil.offsetMonth(deliverVehicleDate, offsetMonths);
            }
            if (null != offsetDays) {
                deliverVehicleDate = DateUtil.offsetDay(deliverVehicleDate, offsetDays);
            }
            return DateUtil.formatDate(deliverVehicleDate);
        }
    }
}
