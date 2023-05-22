package com.mfexpress.rent.deliver.domain;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.mfexpress.base.starter.logback.log.PrintParam;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.redis.RedisTools;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.po.ElectronicHandoverContractPO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.vo.GroupPhotoVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.*;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverInvalidCmd;
import com.mfexpress.rent.deliver.dto.entity.RecoverAbnormal;
import com.mfexpress.rent.deliver.dto.entity.RecoverVehicle;
import com.mfexpress.rent.deliver.entity.DeliverEntity;
import com.mfexpress.rent.deliver.entity.DeliverVehicleEntity;
import com.mfexpress.rent.deliver.entity.RecoverVehicleEntity;
import com.mfexpress.rent.deliver.entity.ServeEntity;
import com.mfexpress.rent.deliver.entity.api.RecoverVehicleEntityApi;
import com.mfexpress.rent.deliver.gateway.*;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private RecoverVehicleEntityApi recoverVehicleEntityApi;

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
        DeliverEntity deliver = deliverGateway.getDeliverByServeNo(recoverVehicle.getServeNo());
        if (null == deliver) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "交付单查询失败");
        }
        BeanUtils.copyProperties(recoverVehicleDTO, recoverVehicle);
        recoverVehicle.setStatus(ValidStatusEnum.INVALID.getCode());
        recoverVehicle.setServeNo(null);
        recoverVehicle.setDeliverNo(deliver.getDeliverNo());
        recoverVehicleGateway.updateRecoverVehicleByDeliverNo(recoverVehicle);
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
        DeliverEntity deliver = deliverGateway.getDeliverByServeNo(cmd.getServeNo());
        if (null == deliver) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "交付单查询失败");
        }
        RecoverVehicleEntity recoverVehicle = new RecoverVehicleEntity();
        recoverVehicle.setDeliverNo(deliver.getDeliverNo());
        recoverVehicle.setParkFee(cmd.getParkFee());
        recoverVehicle.setDamageFee(cmd.getDamageFee());
        int i = recoverVehicleGateway.updateRecoverVehicleByDeliverNo(recoverVehicle);
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
        deliverToUpdate.setIsInsurance(JudgeEnum.YES.getCode());
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
        // recoverVehicle.setServeNo(cmd.getServeNo());
        recoverVehicle.setDeliverNo(deliver.getDeliverNo());
        recoverVehicle.setContactsName(elecContractDTO.getContactsName());
        recoverVehicle.setContactsCard(elecContractDTO.getContactsCard());
        recoverVehicle.setContactsPhone(elecContractDTO.getContactsPhone());
        recoverVehicle.setRecoverVehicleTime(cmd.getRecoverTime());
        recoverVehicle.setDamageFee(elecContractDTO.getRecoverDamageFee());
        recoverVehicle.setParkFee(elecContractDTO.getRecoverParkFee());
        recoverVehicle.setWareHouseId(elecContractDTO.getRecoverWareHouseId());
        List<GroupPhotoVO> groupPhotoVOS = JSONUtil.toList(elecContractDTO.getPlateNumberWithImgs(), GroupPhotoVO.class);
        recoverVehicle.setImgUrl(groupPhotoVOS.get(0).getImgUrl());
        recoverVehicleGateway.updateRecoverVehicleByDeliverNo(recoverVehicle);

        return Result.getInstance(0).success();
    }

    @Override
    @PostMapping("/recovered")
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> recovered(@RequestParam("deliverNo") String deliverNo, @RequestParam("foreignNo") String foreignNo) {
        // 判断deliver中的合同状态，如果不是签署中状态，不可进行此操作
        log.info("recovered---->deliver---->{} 开始修改", deliverNo);
        DeliverEntity deliver = deliverGateway.getDeliverByDeliverNo(deliverNo);
        if (DeliverContractStatusEnum.SIGNING.getCode() != deliver.getRecoverContractStatus()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单当前状态不允许收车");
        }

        // deliver 收车签署状态改为已完成，并且状态改为已收车
        DeliverEntity deliverToUpdate = new DeliverEntity();
        deliverToUpdate.setDeliverNo(deliver.getDeliverNo());
        deliverToUpdate.setRecoverContractStatus(DeliverContractStatusEnum.COMPLETED.getCode());
        deliverToUpdate.setDeliverStatus(DeliverEnum.RECOVER.getCode());
        //2023-0516 收车签署成功后不在操作保险
        deliverToUpdate.setIsInsurance(JudgeEnum.YES.getCode());
        deliverGateway.updateDeliverByDeliverNos(Collections.singletonList(deliver.getDeliverNo()), deliverToUpdate);
        log.info("recovered---->deliver---->{} 结束修改", deliverNo);
        log.info("recovered---->serve---->{} 开始修改", deliver.getServeNo());
        // 服务单状态更改为已收车
        ServeEntity serve = ServeEntity.builder().status(ServeEnum.RECOVER.getCode()).build();
        serveGateway.updateServeByServeNoList(Collections.singletonList(deliver.getServeNo()), serve);
        log.info("recovered---->serve---->{} 结束修改", deliver.getServeNo());

        log.info("recovered---->recoverVehicle---->{} 开始修改", deliver.getCarNum());
        // 用合同信息补充收车单信息
        ElectronicHandoverContractPO contractPO = contractGateway.getContractByForeignNo(foreignNo);
        if (null == contractPO) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "合同编号所属的合同查询失败，" + foreignNo);
        }
        RecoverVehicleEntity recoverVehicle = new RecoverVehicleEntity();
        // recoverVehicle.setServeNo(deliver.getServeNo());
        recoverVehicle.setDeliverNo(deliverNo);
        recoverVehicle.setContactsName(contractPO.getContactsName());
        recoverVehicle.setContactsCard(contractPO.getContactsCard());
        recoverVehicle.setContactsPhone(contractPO.getContactsPhone());
        recoverVehicle.setRecoverVehicleTime(FormatUtil.ymdHmsFormatStringToDate(contractPO.getRecoverVehicleTime()));
        recoverVehicle.setDamageFee(contractPO.getRecoverDamageFee());
        recoverVehicle.setParkFee(contractPO.getRecoverParkFee());
        recoverVehicle.setWareHouseId(contractPO.getRecoverWareHouseId());
        List<GroupPhotoVO> groupPhotoVOS = JSONUtil.toList(contractPO.getPlateNumberWithImgs(), GroupPhotoVO.class);
        recoverVehicle.setImgUrl(groupPhotoVOS.get(0).getImgUrl());
        recoverVehicleGateway.updateRecoverVehicleByDeliverNo(recoverVehicle);
        log.info("recovered---->recoverVehicle---->{} 结束修改", deliver.getCarNum());
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
    public Result<Integer> invalidRecover(RecoverInvalidCmd cmd) {

        return Result.getInstance(recoverVehicleGateway.invalidRecover(cmd)).success();
    }

    @Override
    @PostMapping("/getRecoverVehicleDTOByServeNos")
    @PrintParam
    public Result<List<RecoverVehicleDTO>> getRecoverVehicleDTOByServeNos(@RequestBody List<String> serveNos) {

        return Result.getInstance(recoverVehicleEntityApi.getRecoverVehicleByServeNos(serveNos)).success();

    }
}
