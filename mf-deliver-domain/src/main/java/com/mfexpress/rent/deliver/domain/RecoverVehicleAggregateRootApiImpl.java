package com.mfexpress.rent.deliver.domain;


import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.billing.rentcharge.dto.data.deliver.RecoverVehicleCmd;
import com.mfexpress.billing.rentcharge.dto.data.deliver.RenewalCmd;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.mq.MqTools;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.api.ServeServiceI;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.DeliverContractStatusEnum;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.LeaseModelEnum;
import com.mfexpress.rent.deliver.constant.ReplaceVehicleDepositPayTypeEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.DailyAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.daily.CreateDailyCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.po.ElectronicHandoverContractPO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.GroupPhotoVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverAbnormalCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverAbnormalDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverAbnormalQry;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverDeductionByDeliverCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverDeductionCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverVehicleProcessCmd;
import com.mfexpress.rent.deliver.dto.data.serve.RenewalChargeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.dto.ServeAdjustRecordDTO;
import com.mfexpress.rent.deliver.dto.data.serve.qry.ServeAdjustRecordQry;
import com.mfexpress.rent.deliver.dto.entity.RecoverAbnormal;
import com.mfexpress.rent.deliver.dto.entity.RecoverVehicle;
import com.mfexpress.rent.deliver.entity.DeliverEntity;
import com.mfexpress.rent.deliver.entity.DeliverVehicleEntity;
import com.mfexpress.rent.deliver.entity.RecoverVehicleEntity;
import com.mfexpress.rent.deliver.entity.ServeEntity;
import com.mfexpress.rent.deliver.gateway.DeliverGateway;
import com.mfexpress.rent.deliver.gateway.DeliverVehicleGateway;
import com.mfexpress.rent.deliver.gateway.ElecHandoverContractGateway;
import com.mfexpress.rent.deliver.gateway.RecoverAbnormalGateway;
import com.mfexpress.rent.deliver.gateway.RecoverVehicleGateway;
import com.mfexpress.rent.deliver.gateway.ServeGateway;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import com.mfexpress.rent.deliver.utils.MainServeUtil;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.constant.MaintenanceTypeEnum;
import com.mfexpress.rent.maintain.dto.data.MaintenanceDTO;
import com.mfexpress.rent.maintain.dto.data.ReplaceVehicleDTO;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.WarehouseAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidStockStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.rent.vehicle.data.dto.warehouse.WarehouseDto;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/domain/deliver/v3/recovervehicle")
@Api(tags = "domain--交付--1.4收车单聚合")
public class RecoverVehicleAggregateRootApiImpl implements RecoverVehicleAggregateRootApi {

    @Resource
    private RecoverVehicleGateway recoverVehicleGateway;

    @Resource
    private RedisTools redisTools;

    @Resource
    private DeliverGateway deliverGateway;

    @Resource
    private RecoverAbnormalGateway recoverAbnormalGateway;

    @Resource
    private DeliverVehicleGateway deliverVehicleGateway;

    @Resource
    private ServeGateway serveGateway;

    @Resource
    private ElecHandoverContractGateway contractGateway;

    @Resource
    private WarehouseAggregateRootApi warehouseAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private MaintenanceAggregateRootApi maintenanceAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private DailyAggregateRootApi dailyAggregateRootApi;

    @Resource
    private ServeServiceI serveServiceI;

    @Resource
    private MqTools mqTools;

    @Value("${rocketmq.listenEventTopic}")
    private String event;

    @Override
    @PostMapping("/getRecoverVehicleDtoByDeliverNo")
    @PrintParam
    public Result<RecoverVehicleDTO> getRecoverVehicleDtoByDeliverNo(@RequestParam("deliverNo") String deliverNo) {
        RecoverVehicleEntity recoverVehicle = recoverVehicleGateway.getRecoverVehicleByDeliverNo(deliverNo);
        RecoverVehicleDTO recoverVehicleDTO = new RecoverVehicleDTO();
        if (recoverVehicle != null) {
            BeanUtils.copyProperties(recoverVehicle, recoverVehicleDTO);
            return Result.getInstance(recoverVehicleDTO).success();
        }
        // 查询有效的收车单
        return Result.getInstance((RecoverVehicleDTO) null).success();
    }

    @Override
    @PostMapping("/addRecoverVehicle")
    @PrintParam
    public Result<String> addRecoverVehicle(@RequestBody List<RecoverVehicleDTO> recoverVehicleDTOList) {
        if (recoverVehicleDTOList != null) {
            List<RecoverVehicleEntity> recoverVehicleList = recoverVehicleDTOList.stream().map(recoverVehicleDTO -> {
                long incr = redisTools.incr(DeliverUtils.getEnvVariable(Constants.REDIS_RECOVER_VEHICLE_KEY) + DeliverUtils.getDateByYYMMDD(new Date()), 1);
                String recoverVehicleNo = DeliverUtils.getNo(Constants.REDIS_RECOVER_VEHICLE_KEY, incr);
                RecoverVehicleEntity recoverVehicle = new RecoverVehicleEntity();
                BeanUtils.copyProperties(recoverVehicleDTO, recoverVehicle);
                recoverVehicle.setRecoverVehicleNo(recoverVehicleNo);
                return recoverVehicle;
            }).collect(Collectors.toList());
            recoverVehicleGateway.addRecoverVehicle(recoverVehicleList);
        }

        return Result.getInstance("申请收车成功").success();
    }

    @Override
    @PostMapping("/cancelRecover")
    @PrintParam
    public Result<String> cancelRecover(@RequestBody RecoverVehicleDTO recoverVehicleDTO) {
        RecoverVehicleEntity recoverVehicle = new RecoverVehicleEntity();
        BeanUtils.copyProperties(recoverVehicleDTO, recoverVehicle);
        recoverVehicle.setStatus(ValidStatusEnum.INVALID.getCode());
        recoverVehicleGateway.updateRecoverVehicle(recoverVehicle);
        return Result.getInstance("").success();
    }

    // 这里的验车是补充收车单信息
    /*@Override
    @PostMapping("/toCheck")
    @PrintParam
    public Result<String> toCheck(@RequestBody RecoverVehicleDTO recoverVehicleDTO) {
        RecoverVehicle recoverVehicle = new RecoverVehicle();
        BeanUtils.copyProperties(recoverVehicleDTO, recoverVehicle);
        int i = recoverVehicleGateway.updateRecoverVehicle(recoverVehicle);
        return i > 0 ? Result.getInstance("验车成功").success() : Result.getInstance("验车失败").fail(-1, "验车失败");

    }*/

    @Override
    @PostMapping("/toBackInsure")
    @PrintParam
    public Result<List<RecoverVehicleDTO>> toBackInsure(@RequestBody List<String> serveNoList) {
        List<RecoverVehicleEntity> recoverVehicleList = recoverVehicleGateway.selectRecoverByServeNoList(serveNoList);
        List<RecoverVehicleDTO> recoverVehicleDTOList = new LinkedList<>();
        if (recoverVehicleList != null) {
            recoverVehicleDTOList = recoverVehicleList.stream().map(recoverVehicle -> {
                RecoverVehicleDTO recoverVehicleDTO = new RecoverVehicleDTO();
                BeanUtils.copyProperties(recoverVehicle, recoverVehicleDTO);
                return recoverVehicleDTO;
            }).collect(Collectors.toList());
            return Result.getInstance(recoverVehicleDTOList);
        }

        return Result.getInstance(recoverVehicleDTOList).success();
    }

    @Override
    @PostMapping("getRecoverVehicleByServeNo")
    public Result<Map<String, RecoverVehicle>> getRecoverVehicleByServeNo(@RequestBody List<String> serveNoList) {
        List<RecoverVehicleEntity> recoverVehicleList = recoverVehicleGateway.selectRecoverByServeNoList(serveNoList);
        Map<String, RecoverVehicle> recoverVehicleMap = new HashMap<>();
        recoverVehicleList.forEach(recoverVehicleEntity -> {
            RecoverVehicle recoverVehicle = BeanUtil.copyProperties(recoverVehicleEntity, RecoverVehicle.class);
            recoverVehicleMap.put(recoverVehicleEntity.getServeNo(), recoverVehicle);
        });

        return Result.getInstance(recoverVehicleMap).success();
    }

    @Override
    @PostMapping("/updateDeductionFee")
    public Result<Integer> updateDeductionFee(@RequestBody RecoverDeductionCmd cmd) {
        RecoverVehicleEntity recoverVehicle = new RecoverVehicleEntity();
        recoverVehicle.setServeNo(cmd.getServeNo());
        recoverVehicle.setParkFee(cmd.getParkFee());
        recoverVehicle.setDamageFee(cmd.getDamageFee());
        int i = recoverVehicleGateway.updateRecoverVehicle(recoverVehicle);
        if (i <= 0) {
            return Result.getInstance((Integer) null).fail(-1, "修改失败");

        }
        return Result.getInstance(i).success();
    }

    @Override
    @PostMapping("/getRecoverVehicleDtosByDeliverNoList")
    @PrintParam
    public Result<List<RecoverVehicleDTO>> getRecoverVehicleDtosByDeliverNoList(@RequestParam("deliverNoList") List<String> deliverNoList) {
        List<RecoverVehicleEntity> recoverVehicleDtosByDeliverNoList = recoverVehicleGateway.getRecoverVehicleByDeliverNoList(deliverNoList);
        if (CollectionUtil.isEmpty(recoverVehicleDtosByDeliverNoList)) {
            return Result.getInstance((List<RecoverVehicleDTO>) null).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
        List<RecoverVehicleDTO> recoverVehicleDTOS = BeanUtil.copyToList(recoverVehicleDtosByDeliverNoList, RecoverVehicleDTO.class, CopyOptions.create());
        return Result.getInstance(recoverVehicleDTOS).success();
    }

    @Override
    public Result<List<RecoverVehicleDTO>> getRecoverVehicleDTOByDeliverNos(@RequestParam("deliverNoList") List<String> deliverNoList) {
        List<RecoverVehicleEntity> recoverVehicleDtosByDeliverNoList = recoverVehicleGateway.getRecoverVehicleByDeliverNos(deliverNoList);
        if (CollectionUtil.isEmpty(recoverVehicleDtosByDeliverNoList)) {
            return Result.getInstance((List<RecoverVehicleDTO>) null).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
        List<RecoverVehicleDTO> recoverVehicleDTOS = BeanUtil.copyToList(recoverVehicleDtosByDeliverNoList, RecoverVehicleDTO.class, CopyOptions.create());
        return Result.getInstance(recoverVehicleDTOS).success();
    }

    @Override
    @PostMapping("/abnormalRecover")
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> abnormalRecover(@RequestBody @Validated RecoverAbnormalCmd cmd) {
        // 判断deliver中的合同状态，如果不是签署中和生成中状态，不可进行此操作
        DeliverEntity deliver = deliverGateway.getDeliverByServeNo(cmd.getServeNo());
        if (DeliverContractStatusEnum.GENERATING.getCode() != deliver.getRecoverContractStatus() && DeliverContractStatusEnum.SIGNING.getCode() != deliver.getRecoverContractStatus()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单当前状态不允许异常收车");
        }

        DeliverVehicleEntity deliverVehicle = deliverVehicleGateway.getDeliverVehicleByDeliverNo(deliver.getDeliverNo());
        if (null == deliverVehicle) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "发车单查询失败");
        }
        Date deliverVehicleTime = deliverVehicle.getDeliverVehicleTime();
        if (!deliverVehicleTime.equals(cmd.getRecoverTime())) {
            if (!deliverVehicleTime.before(cmd.getRecoverTime())) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "收车日期不能早于发车日期");
            }
        }

        DateTime endDate = DateUtil.endOfMonth(new Date());
        DateTime startDate = DateUtil.beginOfMonth(new Date());
        //增加收车日期限制
        if (!endDate.isAfter(cmd.getRecoverTime()) || cmd.getRecoverTime().before(startDate)) {
            throw new CommonException(ResultErrorEnum.UPDATE_ERROR.getCode(), "收车日期请选择在当月内");
        }

        // deliver 收车签署状态改为未签，并且异常收车flag改为真，状态改为已收车
        DeliverEntity deliverToUpdate = new DeliverEntity();
        deliverToUpdate.setDeliverNo(deliver.getDeliverNo());
        deliverToUpdate.setRecoverContractStatus(DeliverContractStatusEnum.NOSIGN.getCode());
        deliverToUpdate.setRecoverAbnormalFlag(JudgeEnum.YES.getCode());
        deliverToUpdate.setDeliverStatus(DeliverEnum.RECOVER.getCode());
        deliverToUpdate.setCarServiceId(cmd.getOperatorId());
        deliverToUpdate.setUpdateId(cmd.getOperatorId());
        deliverGateway.updateDeliverByDeliverNos(Collections.singletonList(deliver.getDeliverNo()), deliverToUpdate);

        // 服务单状态更改为已收车
        ServeEntity serve = ServeEntity.builder().status(ServeEnum.RECOVER.getCode()).build();
        serveGateway.updateServeByServeNoList(Collections.singletonList(cmd.getServeNo()), serve);

        // 补充异常收车信息
        RecoverAbnormal recoverAbnormal = new RecoverAbnormal();
        BeanUtils.copyProperties(cmd, recoverAbnormal);
        recoverAbnormal.setDeliverNo(deliver.getDeliverNo());
        recoverAbnormal.setCause(cmd.getReason());
        recoverAbnormal.setImgUrl(JSONUtil.toJsonStr(cmd.getImgUrls()));
        recoverAbnormal.setCreatorId(cmd.getOperatorId());
        recoverAbnormal.setCreateTime(cmd.getRecoverTime());
        recoverAbnormalGateway.create(recoverAbnormal);

        // 取出合同信息修改收车单
        ElecContractDTO elecContractDTO = cmd.getElecContractDTO();
        RecoverVehicleEntity recoverVehicle = new RecoverVehicleEntity();
        recoverVehicle.setServeNo(cmd.getServeNo());
        recoverVehicle.setContactsName(elecContractDTO.getContactsName());
        recoverVehicle.setContactsCard(elecContractDTO.getContactsCard());
        recoverVehicle.setContactsPhone(elecContractDTO.getContactsPhone());
        recoverVehicle.setRecoverVehicleTime(cmd.getRecoverTime());
        recoverVehicle.setDamageFee(elecContractDTO.getRecoverDamageFee());
        recoverVehicle.setParkFee(elecContractDTO.getRecoverParkFee());
        recoverVehicle.setWareHouseId(elecContractDTO.getRecoverWareHouseId());
        List<GroupPhotoVO> groupPhotoVOS = JSONUtil.toList(elecContractDTO.getPlateNumberWithImgs(), GroupPhotoVO.class);
        recoverVehicle.setImgUrl(groupPhotoVOS.get(0).getImgUrl());
        recoverVehicleGateway.updateRecoverVehicle(recoverVehicle);

        return Result.getInstance(0).success();
    }

    @Override
    @PostMapping("/recovered")
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> recovered(@RequestParam("deliverNo") String deliverNo, @RequestParam("foreignNo") String foreignNo) {
        // 判断deliver中的合同状态，如果不是签署中状态，不可进行此操作
        DeliverEntity deliver = deliverGateway.getDeliverByDeliverNo(deliverNo);
        if (DeliverContractStatusEnum.SIGNING.getCode() != deliver.getRecoverContractStatus()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单当前状态不允许收车");
        }

        // deliver 收车签署状态改为已完成，并且状态改为已收车
        DeliverEntity deliverToUpdate = new DeliverEntity();
        deliverToUpdate.setDeliverNo(deliver.getDeliverNo());
        deliverToUpdate.setRecoverContractStatus(DeliverContractStatusEnum.COMPLETED.getCode());
        deliverToUpdate.setDeliverStatus(DeliverEnum.RECOVER.getCode());
        deliverGateway.updateDeliverByDeliverNos(Collections.singletonList(deliver.getDeliverNo()), deliverToUpdate);

        // 服务单状态更改为已收车
        ServeEntity serve = ServeEntity.builder().status(ServeEnum.RECOVER.getCode()).build();
        serveGateway.updateServeByServeNoList(Collections.singletonList(deliver.getServeNo()), serve);

        // 用合同信息补充收车单信息
        ElectronicHandoverContractPO contractPO = contractGateway.getContractByForeignNo(foreignNo);
        if (null == contractPO) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "合同编号所属的合同查询失败，" + foreignNo);
        }
        RecoverVehicleEntity recoverVehicle = new RecoverVehicleEntity();
        recoverVehicle.setServeNo(deliver.getServeNo());
        recoverVehicle.setContactsName(contractPO.getContactsName());
        recoverVehicle.setContactsCard(contractPO.getContactsCard());
        recoverVehicle.setContactsPhone(contractPO.getContactsPhone());
        recoverVehicle.setRecoverVehicleTime(FormatUtil.ymdHmsFormatStringToDate(contractPO.getRecoverVehicleTime()));
        recoverVehicle.setDamageFee(contractPO.getRecoverDamageFee());
        recoverVehicle.setParkFee(contractPO.getRecoverParkFee());
        recoverVehicle.setWareHouseId(contractPO.getRecoverWareHouseId());
        List<GroupPhotoVO> groupPhotoVOS = JSONUtil.toList(contractPO.getPlateNumberWithImgs(), GroupPhotoVO.class);
        recoverVehicle.setImgUrl(groupPhotoVOS.get(0).getImgUrl());
        recoverVehicleGateway.updateRecoverVehicle(recoverVehicle);

        return Result.getInstance(0).success();
    }

    @Override
    @PostMapping("/getRecoverAbnormalByQry")
    @PrintParam
    public Result<RecoverAbnormalDTO> getRecoverAbnormalByQry(@RequestBody RecoverAbnormalQry qry) {
        RecoverAbnormal recoverAbnormal = recoverAbnormalGateway.getRecoverAbnormalByServeNo(qry.getServeNo());
        if (null == recoverAbnormal) {
            return Result.getInstance((RecoverAbnormalDTO) null).success();
        }
        RecoverAbnormalDTO recoverAbnormalDTO = new RecoverAbnormalDTO();
        BeanUtils.copyProperties(recoverAbnormal, recoverAbnormalDTO);
        return Result.getInstance(recoverAbnormalDTO).success();
    }

    @Override
    @PostMapping("/getRecentlyHistoryMapByServeNoList")
    @PrintParam
    public Result<Map<String, RecoverVehicleDTO>> getRecentlyHistoryMapByServeNoList(@RequestParam("reactiveServeNoList") List<String> reactiveServeNoList) {
        if (reactiveServeNoList.isEmpty()) {
            return Result.getInstance((Map<String, RecoverVehicleDTO>) null).fail(ResultErrorEnum.VILAD_ERROR.getCode(), ResultErrorEnum.VILAD_ERROR.getName());
        }
        List<DeliverEntity> deliverEntityList = deliverGateway.getHistoryListByServeNoList(reactiveServeNoList);
        Map<String, DeliverEntity> deliverEntityMap = deliverEntityList.stream().collect(Collectors.toMap(DeliverEntity::getServeNo, Function.identity(), (v1, v2) -> v2));
        List<String> deliverNos = deliverEntityMap.values().stream().map(DeliverEntity::getDeliverNo).collect(Collectors.toList());
        List<RecoverVehicleEntity> recoverVehicleEntityList = recoverVehicleGateway.getRecoverVehicleByDeliverNoList(deliverNos);
        Map<String, RecoverVehicleDTO> recoverVehicleDTOMap = recoverVehicleEntityList.stream().map(recoverVehicleEntity -> {
            RecoverVehicleDTO recoverVehicleDTO = new RecoverVehicleDTO();
            BeanUtils.copyProperties(recoverVehicleEntity, recoverVehicleDTO);
            return recoverVehicleDTO;
        }).collect(Collectors.toMap(RecoverVehicleDTO::getServeNo, Function.identity(), (v1, v2) -> v2));

        return Result.getInstance(recoverVehicleDTOMap).success();
    }

    @Override
    @PostMapping("/updateDeductionFeeByDeliver")
    @PrintParam
    public Result<Integer> updateDeductionFeeByDeliver(@RequestBody @Validated RecoverDeductionByDeliverCmd cmd) {
        RecoverVehicleEntity recoverVehicle = new RecoverVehicleEntity();
        recoverVehicle.setDeliverNo(cmd.getDeliverNo());
        recoverVehicle.setParkFee(cmd.getParkFee());
        recoverVehicle.setDamageFee(cmd.getDamageFee());
        recoverVehicleGateway.updateRecoverVehicleByDeliverNo(recoverVehicle);
        return Result.getInstance(0).success();
    }

    @Override
    @PostMapping(value = "/recover/process")
    @Transactional(rollbackFor = Exception.class)
    @PrintParam
    public Result<List<String>> recoverVehicleProcess(RecoverVehicleProcessCmd cmd) {
        List<String> serveNoList = new LinkedList<>();
        // 交付单、服务单修改
        Result<Integer> recoveredResult = recovered(cmd.getDeliverNo(), cmd.getContractForeignNo());
        ResultValidUtils.checkResultException(recoveredResult);

        //更新车辆状态
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setId(Collections.singletonList(cmd.getCarId()));
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.UNCHECKED.getCode());
        vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.IN.getCode());
        vehicleSaveCmd.setWarehouseId(cmd.getRecoverWareHouseId());
        vehicleSaveCmd.setCustomerId(0);
        Result<WarehouseDto> wareHouseResult = warehouseAggregateRootApi.getWarehouseById(vehicleSaveCmd.getWarehouseId());
        if (wareHouseResult.getData() != null) {
            vehicleSaveCmd.setAddress(wareHouseResult.getData().getName());
        }
        Result<String> changeVehicleStatusResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if (ResultErrorEnum.SUCCESSED.getCode() != changeVehicleStatusResult.getCode()) {
            log.error("收车电子合同签署完成时，更改车辆状态失败，serveNo：{}，车辆id：{}", cmd.getServeNo(), cmd.getCarId());
        }

        // 判断实际收车日期和预计收车日期的前后关系，如果实际收车日期在预计收车日期之前或当天，发送收车计费消息，反之，发送自动续约消息

        Date recoverVehicleTime = cmd.getRecoverVehicleTime();
        String expectRecoverDateChar = cmd.getExpectRecoverDate();
        DateTime expectRecoverDate = DateUtil.parseDate(expectRecoverDateChar);
        // 发送收车计费消息
        if (expectRecoverDate.isAfterOrEquals(recoverVehicleTime)) {
            //收车计费
            RecoverVehicleCmd recoverVehicleCmd = new RecoverVehicleCmd();
            recoverVehicleCmd.setServeNo(cmd.getServeNo());
            recoverVehicleCmd.setVehicleId(cmd.getCarId());
            recoverVehicleCmd.setDeliverNo(cmd.getDeliverNo());
            recoverVehicleCmd.setCustomerId(cmd.getCustomerId());
            recoverVehicleCmd.setCreateId(cmd.getOperatorId());
            recoverVehicleCmd.setRecoverDate(DateUtil.formatDate(cmd.getRecoverVehicleTime()));
            log.info("正常收车时，交付域向计费域发送的收车单信息：{}", recoverVehicleCmd);
            mqTools.send(event, "recover_vehicle", null, JSON.toJSONString(recoverVehicleCmd));

            // 服务单维修中
            if (ServeEnum.REPAIR.getCode().equals(cmd.getServeStatus())) {
                // 查找维修单
                Result<MaintenanceDTO> maintenanceDTOResult = maintenanceAggregateRootApi.getMaintenanceByServeNo(cmd.getServeNo());
                MaintenanceDTO maintenanceDTO = ResultDataUtils.getInstance(maintenanceDTOResult).getDataOrException();
                // 维修性质为故障维修
                if (MaintenanceTypeEnum.FAULT.getCode().intValue() == maintenanceDTO.getType()) {
                    // 查询替换车服务单
                    ReplaceVehicleDTO replaceVehicleDTO = MainServeUtil.getReplaceVehicleDTOBySourceServNo(maintenanceAggregateRootApi, cmd.getServeNo());
                    Result<ServeDTO> replaceServeDTOResult = serveAggregateRootApi.getServeDtoByServeNo(replaceVehicleDTO.getServeNo());
                    ServeDTO replaceServe = ResultDataUtils.getInstance(replaceServeDTOResult).getDataOrException();
                    // 替换单已发车且变更为正常服务单
                    if (Optional.ofNullable(replaceServe)
                            .filter(o -> ServeEnum.DELIVER.getCode().equals(o.getStatus())
                                    && JudgeEnum.NO.getCode().equals(o.getReplaceFlag())
                                    && LeaseModelEnum.NORMAL.getCode() == o.getLeaseModelId()).isPresent()) {
                        // 原车维修单变为库存中维修
                        maintenanceAggregateRootApi.updateMaintenanceDetailByServeNo(cmd.getServeNo());
                        // 替换车押金支付
                        // 查询替换单支付方式
                        ServeAdjustRecordQry qry = new ServeAdjustRecordQry();
                        qry.setServeNo(replaceServe.getServeNo());
                        Result<ServeAdjustRecordDTO> serveAdjustRecordDTOResult = serveAggregateRootApi.getServeAdjustRecord(qry);
                        ServeAdjustRecordDTO serveAdjustRecordDTO = ResultDataUtils.getInstance(serveAdjustRecordDTOResult).getDataOrException();

                        if (ReplaceVehicleDepositPayTypeEnum.ACCOUNT_DEPOSIT_UNLOCK_PAY.getCode() == serveAdjustRecordDTO.getDepositPayType()) {
                            // 账本扣除
                            serveServiceI.serveDepositPay(replaceServe, cmd.getOperatorId());
                        }

                        // 替换车开始计费
                        Result<DeliverDTO> replaceDeliverResult = deliverAggregateRootApi.getDeliverByServeNo(replaceServe.getServeNo());
                        DeliverDTO replaceDeliver = ResultDataUtils.getInstance(replaceDeliverResult).getDataOrException();
                        RenewalCmd renewalCmd = new RenewalCmd();
                        renewalCmd.setServeNo(replaceServe.getServeNo());
                        renewalCmd.setDeliverNo(replaceDeliver.getDeliverNo());
                        renewalCmd.setVehicleId(replaceDeliver.getCarId());
                        renewalCmd.setCustomerId(replaceServe.getCustomerId());
                        renewalCmd.setRent(replaceServe.getRent());
                        renewalCmd.setRentRatio(replaceServe.getRentRatio().doubleValue());

//                        renewalCmd.setRenewalDate();
                        renewalCmd.setCreateId(cmd.getOperatorId());
                        renewalCmd.setRentEffectDate(FormatUtil.ymdFormatDateToString(new Date()));
                        renewalCmd.setEffectFlag(true);
                        mqTools.send(event, "price_change", null, JSON.toJSONString(renewalCmd));
                    }
                }
            }
        } else {
            // 发送自动续约消息
            RenewalChargeCmd renewalChargeCmd = new RenewalChargeCmd();
            renewalChargeCmd.setServeNo(cmd.getServeNo());
            renewalChargeCmd.setCreateId(cmd.getOperatorId());
            renewalChargeCmd.setCustomerId(cmd.getCustomerId());
            renewalChargeCmd.setDeliverNo(cmd.getDeliverNo());
            renewalChargeCmd.setVehicleId(cmd.getCarId());
            renewalChargeCmd.setEffectFlag(false);
            // 续约目标日期为实际收车日期
            renewalChargeCmd.setRenewalDate(DateUtil.formatDate(recoverVehicleTime));
            mqTools.send(event, "renewal_fee", null, JSON.toJSONString(renewalChargeCmd));
        }

        serveNoList.add(cmd.getServeNo());

        CreateDailyCmd createDailyCmd = new CreateDailyCmd();
        createDailyCmd.setDeliverFlag(0);
        createDailyCmd.setDeliverRecoverDate(cmd.getRecoverVehicleTime());
        createDailyCmd.setServeNoList(serveNoList);

        dailyAggregateRootApi.createDaily(createDailyCmd);

        return Result.getInstance(serveNoList).success();
    }
}
