package com.mfexpress.rent.deliver.domain;


import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.RedisTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.entity.RecoverVehicle;
import com.mfexpress.rent.deliver.gateway.RecoverVehicleGateway;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import io.swagger.annotations.Api;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/domain/deliver/v3/recovervehicle")
@Api(tags = "domain--交付--1.4收车单聚合")
public class RecoverVehicleAggregateRootApiImpl implements RecoverVehicleAggregateRootApi {

    @Resource
    private RecoverVehicleGateway recoverVehicleGateway;
    @Resource
    private RedisTools redisTools;

    @Override
    @PostMapping("/getRecoverVehicleDtoByDeliverNo")
    @PrintParam
    public Result<RecoverVehicleDTO> getRecoverVehicleDtoByDeliverNo(@RequestParam("deliverNo") String deliverNo) {
        RecoverVehicle recoverVehicle = recoverVehicleGateway.getRecoverVehicleByDeliverNo(deliverNo);
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
            List<RecoverVehicle> recoverVehicleList = recoverVehicleDTOList.stream().map(recoverVehicleDTO -> {
                long incr = redisTools.incr(DeliverUtils.getEnvVariable(Constants.REDIS_RECOVER_VEHICLE_KEY) + DeliverUtils.getDateByYYMMDD(new Date()), 1);
                String recoverVehicleNo = DeliverUtils.getNo(Constants.REDIS_RECOVER_VEHICLE_KEY, incr);
                RecoverVehicle recoverVehicle = new RecoverVehicle();
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
        RecoverVehicle recoverVehicle = new RecoverVehicle();
        BeanUtils.copyProperties(recoverVehicleDTO, recoverVehicle);
        recoverVehicle.setStatus(ValidStatusEnum.INVALID.getCode());
        recoverVehicleGateway.updateRecoverVehicle(recoverVehicle);
        return Result.getInstance("").success();
    }

    @Override
    @PostMapping("/toCheck")
    @PrintParam
    public Result<String> toCheck(@RequestBody RecoverVehicleDTO recoverVehicleDTO) {
        RecoverVehicle recoverVehicle = new RecoverVehicle();
        BeanUtils.copyProperties(recoverVehicleDTO, recoverVehicle);
        int i = recoverVehicleGateway.updateRecoverVehicle(recoverVehicle);
        return i > 0 ? Result.getInstance("验车成功").success() : Result.getInstance("验车失败").fail(-1, "验车失败");

    }

    @Override
    @PostMapping("/toBackInsure")
    @PrintParam
    public Result<List<RecoverVehicleDTO>> toBackInsure(@RequestBody List<String> serveNoList) {
        List<RecoverVehicle> recoverVehicleList = recoverVehicleGateway.selectRecoverByServeNoList(serveNoList);
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
}
