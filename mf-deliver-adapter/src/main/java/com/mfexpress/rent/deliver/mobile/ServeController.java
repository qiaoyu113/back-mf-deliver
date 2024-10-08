package com.mfexpress.rent.deliver.mobile;

import com.mfexpress.component.constants.CommonConstants;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.base.starter.logback.log.PrintParam;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.token.TokenTools;
import com.mfexpress.rent.deliver.api.RecoverVehicleServiceI;
import com.mfexpress.rent.deliver.api.ServeServiceI;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.DeliverNoCmd;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import com.mfexpress.rent.deliver.scheduler.ServeDailyScheduler;
import com.mfexpress.rent.deliver.scheduler.ServeRenewalScheduler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/deliver/v3/serve")
@Api(tags = "api--交付--1.4租赁服务单", value = "ServeController")
@ApiSort(1)
public class ServeController {

    @Resource
    private ServeServiceI serveServiceI;

    @Resource
    private ServeRenewalScheduler serveRenewalScheduler;
    @Resource
    private ServeDailyScheduler serveDailyScheduler;

    @Resource
    private RecoverVehicleServiceI recoverVehicleServiceI;


    //====================租赁服务单生成===============//

    @ApiOperation("生成租赁服务单")
    @PostMapping("/addServe")
    @PrintParam
    public Result<String> addServe(@RequestBody ServeAddCmd serveAddCmd) {

        return Result.getInstance(serveServiceI.addServe(serveAddCmd));
    }
    //====================发车任务=======================//

    @PostMapping("/getServeDeliverTaskListVO")
    @ApiOperation("发车任务列表")
    @PrintParam
    public Result<ServeDeliverTaskListVO> getServeStayDeliverTaskListVO(@RequestBody ServeDeliverTaskQryCmd serveDeliverTaskQryCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {

        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            //提示失败结果
            return Result.getInstance((ServeDeliverTaskListVO) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        return Result.getInstance(serveServiceI.getServeDeliverTaskListVO(serveDeliverTaskQryCmd, tokenInfo));
    }


    @PostMapping("/getServeFastPreselectedVO")
    @ApiOperation("订单快速预选页")
    @PrintParam
    public Result<ServeFastPreselectedListVO> getServeFastPreselectedVO(@RequestBody ServeQryListCmd serveQryListCmd) {

        return Result.getInstance(serveServiceI.getServeFastPreselectedVO(serveQryListCmd)).success();
    }

    //===================发车操作列表======================//


    @Valid
    @PostMapping("/getServeListVoByOrderNoAll")
    @ApiOperation("全部发车操作列表")
    public Result<ServeListVO> getServeListVoByOrderNoAll(@RequestBody ServeQryListCmd serveQryListCmd) {

        return Result.getInstance(serveServiceI.getServeListVoByOrderNoAll(serveQryListCmd)).success();
    }


    @Valid
    @PostMapping("/getServeListVoPreselected")
    @ApiOperation("发车操作待预选列表")
    @PrintParam
    public Result<ServePreselectedListVO> getServeListVoPreselected(@RequestBody ServeQryListCmd serveQryListCmd) {

        return Result.getInstance(serveServiceI.getServeListVoPreselected(serveQryListCmd)).success();
    }


    @Valid
    @PostMapping("/getServeListVoInsure")
    @ApiOperation("发车操作待投保列表")
    public Result<ServeListVO> getServeListVoInsure(@RequestBody ServeQryListCmd serveQryListCmd) {

        return Result.getInstance(serveServiceI.getServeListVoInsure(serveQryListCmd)).success();
    }

    @Valid
    @PostMapping("/getServeListVoCheck")
    @ApiOperation("发车操作待验车列表")
    @PrintParam
    public Result<ServeListVO> getServeListVoCheck(@RequestBody ServeQryListCmd serveQryListCmd) {

        return Result.getInstance(serveServiceI.getServeListVoCheck(serveQryListCmd)).success();
    }

    @Valid
    @PostMapping("/getServeListVoDeliver")
    @ApiOperation("发车操作待发车列表")
    @PrintParam
    public Result<ServeListVO> getServeListVoDeliver(@RequestBody ServeQryListCmd serveQryListCmd) {

        return Result.getInstance(serveServiceI.getServeListVoDeliver(serveQryListCmd)).success();
    }

    @Valid
    @PostMapping("/getServeListVoCompleted")
    @ApiOperation("发车操作已完成列表")
    @PrintParam
    public Result<ServeListVO> getServeListVoCompleted(@RequestBody ServeQryListCmd serveQryListCmd) {
        return Result.getInstance(serveServiceI.getServeListVoCompleted(serveQryListCmd)).success();
    }

    @PostMapping("/getServeDeliverDetail")
    @ApiOperation("发车服务单详情查询")
    @PrintParam
    public Result<ServeDeliverDetailVO> getServeDeliverDetail(@RequestBody @Validated ServeQryCmd cmd) {
        return Result.getInstance(serveServiceI.getServeDeliverDetail(cmd)).success();
    }

    @PostMapping("/getServeRecoverDetail")
    @ApiOperation("收车服务单详情查询")
    @PrintParam
    public Result<ServeRecoverDetailVO> getServeRecoverDetail(@RequestBody @Validated ServeQryCmd cmd) {
        return Result.getInstance(serveServiceI.getServeRecoverDetail(cmd)).success();
    }

    @PostMapping("/getServeRecoverDetailByDeliver")
    @ApiOperation("通过交付单查询收车服务单")
    @PrintParam
    public Result<ServeRecoverDetailVO> getServeRecoverDetailByDeliver(@RequestBody @Validated ServeQryByDeliverCmd cmd) {
        return Result.getInstance(serveServiceI.getServeRecoverDetailByDeliver(cmd)).success();
    }

    @PostMapping("/getRenewableServeList")
    @ApiOperation("续约合同时客户下的服务单列表的展示/查询")
    @PrintParam
    public Result<List<ServeToRenewalVO>> getRenewableServeList(@RequestBody @Validated RenewableServeQry qry, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(serveServiceI.getRenewableServeList(qry, tokenInfo)).success();
    }

    @PostMapping("/scheduler")
    @ApiOperation("续约合同时客户下的服务单列表的展示/查询")
    @PrintParam
    public Result<Integer> scheduler() {
        serveRenewalScheduler.process();
        return Result.getInstance(0).success();
    }

    @PostMapping("/dailyScheduler")
    @ApiOperation("日报定时接口")
    @PrintParam
    public Result dailyScheduler() {
        serveDailyScheduler.process();
        return Result.getInstance(0).success();
    }
}
