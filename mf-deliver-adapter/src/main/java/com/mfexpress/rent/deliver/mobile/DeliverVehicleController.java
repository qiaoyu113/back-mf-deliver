package com.mfexpress.rent.deliver.mobile;

import com.mfexpress.base.starter.logback.log.PrintParam;
import com.mfexpress.component.constants.CommonConstants;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.token.TokenTools;
import com.mfexpress.rent.deliver.api.DeliverVehicleServiceI;
import com.mfexpress.rent.deliver.config.DeliverProjectProperties;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.CustomerCmd;
import com.mfexpress.transportation.customer.dto.entity.vo.LinkmanVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Api(tags = "api--交付--1.4发车", value = "DeliverVehicleController")
@RequestMapping("/api/deliver/v3/delivervehicle")
@ApiSort(3)
public class DeliverVehicleController {

    @Resource
    private DeliverVehicleServiceI deliverVehicleServiceI;

    @PostMapping("/toDeliver")
    @ApiOperation("发车,在契约锁迭代因发车流程被改变，此接口被废弃")
    @PrintParam
    @Deprecated
    public Result<Integer> toDeliver(@RequestBody DeliverVehicleCmd deliverVehicleCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        //生成发车单 交付单状态更新已发车 初始化操作状态  服务单状态更新为已发车  调用车辆服务为租赁状态
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            //提示失败结果oaContractCode
            return Result.getInstance((Integer) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        // deliverVehicleCmd.setCarServiceId(tokenInfo.getId());
        return Result.getInstance(deliverVehicleServiceI.toDeliver(deliverVehicleCmd, tokenInfo)).success();
    }

    @PostMapping("/selectContactsByDeliverNo")
    @ApiOperation("根据交付单编号查询提车人信息")
    @PrintParam
    public Result<LinkmanVo> selectContactsByDeliverNo(@RequestBody @Validated CustomerCmd customerCmd) {
        return Result.getInstance(deliverVehicleServiceI.getLinkmanByCustomerId(customerCmd.getCustomerId()));
    }

    @PostMapping("/getDeliverVehicleTimeRange")
    @ApiOperation("获取发车时间范围")
    @PrintParam
    public Result<DeliverProjectProperties.TimeRange> getDeliverVehicleTimeRange() {
        return Result.getInstance(DeliverProjectProperties.DELIVER_TIME_RANGE).success();
    }

    @PostMapping("/offlineDeliver")
    @ApiOperation("线下发车")
    @PrintParam
    public Result<Integer> offlineDeliver(@RequestBody @Validated DeliverVehicleCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            return Result.getInstance((Integer) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        return Result.getInstance(deliverVehicleServiceI.offlineDeliver(cmd, tokenInfo)).success();
    }

}
