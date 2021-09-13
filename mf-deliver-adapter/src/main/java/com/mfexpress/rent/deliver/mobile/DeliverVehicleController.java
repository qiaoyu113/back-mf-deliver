package com.mfexpress.rent.deliver.mobile;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.api.DeliverVehicleServiceI;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Api(tags = "api--交付--1.3发车", value = "DeliverVehicleController")
@RequestMapping("/api/deliver/v3/delivervehicle")
@ApiSort(3)
public class DeliverVehicleController {

    @Resource
    private DeliverVehicleServiceI deliverVehicleServiceI;

    @PostMapping("/toDeliver")
    @ApiOperation("发车")
    public Result<String> toDeliver(@RequestBody DeliverVehicleCmd deliverVehicleCmd) {
        //生成发车单 交付单状态更新已发车 初始化操作状态  服务单状态更新为已发车  调用车辆服务为租赁状态

        return Result.getInstance(deliverVehicleServiceI.toDeliver(deliverVehicleCmd)).success();

    }

}
