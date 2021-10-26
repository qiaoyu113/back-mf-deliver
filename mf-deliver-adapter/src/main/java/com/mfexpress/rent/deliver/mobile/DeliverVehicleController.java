package com.mfexpress.rent.deliver.mobile;

import com.mfexpress.component.constants.CommonConstants;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.TokenTools;
import com.mfexpress.rent.deliver.api.DeliverVehicleServiceI;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;
import org.springframework.web.bind.annotation.*;

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
    @PrintParam
    public Result<String> toDeliver(@RequestBody DeliverVehicleCmd deliverVehicleCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        //生成发车单 交付单状态更新已发车 初始化操作状态  服务单状态更新为已发车  调用车辆服务为租赁状态
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            //提示失败结果
            return Result.getInstance((String) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        deliverVehicleCmd.setCarServiceId(tokenInfo.getId());
        return Result.getInstance(deliverVehicleServiceI.toDeliver(deliverVehicleCmd)).success();

    }

}
