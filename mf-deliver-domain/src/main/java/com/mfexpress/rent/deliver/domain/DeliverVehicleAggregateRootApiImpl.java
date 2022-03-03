package com.mfexpress.rent.deliver.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.RedisTools;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverImgInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.entity.Deliver;
import com.mfexpress.rent.deliver.dto.entity.DeliverVehicle;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import com.mfexpress.rent.deliver.gateway.DeliverGateway;
import com.mfexpress.rent.deliver.gateway.DeliverVehicleGateway;
import com.mfexpress.rent.deliver.gateway.ServeGateway;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import io.swagger.annotations.Api;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    @PostMapping("/getDeliverVehicleDto")
    @PrintParam
    public Result<DeliverVehicleDTO> getDeliverVehicleDto(@RequestParam("deliverNo") String deliverNo) {
        DeliverVehicle deliverVehicle = deliverVehicleGateway.getDeliverVehicleByDeliverNo(deliverNo);
        DeliverVehicleDTO deliverVehicleDTO = new DeliverVehicleDTO();
        if (deliverVehicle != null) {
            BeanUtils.copyProperties(deliverVehicle, deliverVehicleDTO);
            return Result.getInstance(deliverVehicleDTO).success();
        }
        return Result.getInstance((DeliverVehicleDTO) null).success();
    }

    @Override
    @PostMapping("/addDeliverVehicle")
    @PrintParam
    public Result<String> addDeliverVehicle(@RequestBody List<DeliverVehicleDTO> deliverVehicleDTOList) {

        if (deliverVehicleDTOList != null) {
            List<DeliverVehicle> deliverVehicleList = deliverVehicleDTOList.stream().map(deliverVehicleDTO -> {
                long incr = redisTools.incr(DeliverUtils.getEnvVariable(Constants.REDIS_DELIVER_VEHICLE_KEY) + DeliverUtils.getDateByYYMMDD(new Date()), 1);
                String deliverVehicleNo = DeliverUtils.getNo(Constants.REDIS_DELIVER_VEHICLE_KEY, incr);
                DeliverVehicle deliverVehicle = new DeliverVehicle();
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
        List<DeliverVehicle> deliverVehicleList = deliverVehicleGateway.getDeliverVehicleByServeNo(serveNoList);
        Map<String, DeliverVehicle> map = deliverVehicleList.stream().collect(Collectors.toMap(DeliverVehicle::getServeNo, Function.identity()));
        return Result.getInstance(map).success();
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
            Serve serve = Serve.builder().status(ServeEnum.DELIVER.getCode()).build();
            String expectRecoverDate = expectRecoverDateMap.get(serveNo);
            if (Objects.nonNull(expectRecoverDate)) {
                serve.setExpectRecoverDate(expectRecoverDate);
            }
            serveGateway.updateServeByServeNo(serveNo, serve);
        }


        // 交付单状态更新为已发车并初始化操作状态
        Deliver deliver = Deliver.builder()
                .deliverStatus(DeliverEnum.DELIVER.getCode())
                .isCheck(JudgeEnum.NO.getCode())
                .isInsurance(JudgeEnum.NO.getCode())
                .deliverContractStatus(DeliverContractStatusEnum.COMPLETED.getCode())
                .build();
        deliverGateway.updateDeliverByServeNoList(serveNoList, deliver);

        // 生成发车单
        List<DeliverVehicle> deliverVehicleList = deliverVehicleDTOList.stream().map(deliverVehicleDTO -> {
            long incr = redisTools.incr(DeliverUtils.getEnvVariable(Constants.REDIS_DELIVER_VEHICLE_KEY) + DeliverUtils.getDateByYYMMDD(new Date()), 1);
            String deliverVehicleNo = DeliverUtils.getNo(Constants.REDIS_DELIVER_VEHICLE_KEY, incr);
            DeliverVehicle deliverVehicle = new DeliverVehicle();
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
        List<DeliverVehicle> deliverList = deliverVehicleGateway.getDeliverVehicleByDeliverNoList(deliverNoList);
        if (CollectionUtil.isEmpty(deliverList)) {
            return Result.getInstance((List<DeliverVehicleDTO>) null).fail(ResultErrorEnum.DATA_NOT_FOUND.getCode(), ResultErrorEnum.DATA_NOT_FOUND.getName());
        }
        List<DeliverVehicleDTO> deliverVehicleDTOList = BeanUtil.copyToList(deliverList, DeliverVehicleDTO.class, new CopyOptions().ignoreError());
        return Result.getInstance(deliverVehicleDTOList).success();
    }

}
