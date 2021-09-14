package com.mfexpress.rent.deliver.domain;

import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.RedisTools;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.entity.DeliverVehicle;
import com.mfexpress.rent.deliver.gateway.DeliverVehicleGateway;
import com.mfexpress.rent.deliver.utils.Utils;
import io.swagger.annotations.Api;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/domain/deliver/v3/delivervehcile")
@Api(tags = "domain--交付--1.3发车单聚合")
public class DeliverVehicleAggregateRootApiImpl implements DeliverVehicleAggregateRootApi {

    @Resource
    private RedisTools redisTools;
    @Resource
    private DeliverVehicleGateway deliverVehicleGateway;


    @Override
    @PostMapping("/getDeliverVehicleDto")
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
    public Result<String> addDeliverVehicle(@RequestBody List<DeliverVehicleDTO> deliverVehicleDTOList) {

        if (deliverVehicleDTOList != null) {
            List<DeliverVehicle> deliverVehicleList = deliverVehicleDTOList.stream().map(deliverVehicleDTO -> {
                long incr = redisTools.incr(Utils.getEnvVariable(Constants.REDIS_DELIVER_VEHICLE_KEY) + Utils.getDateByYYMMDD(new Date()), 1);
                String deliverVehicleNo = Utils.getNo(Constants.REDIS_DELIVER_VEHICLE_KEY, incr);
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
}
