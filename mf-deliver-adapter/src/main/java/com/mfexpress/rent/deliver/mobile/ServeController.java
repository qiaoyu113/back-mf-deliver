package com.mfexpress.rent.deliver.mobile;

import com.mfexpress.component.constants.CommonConstants;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.TokenTools;
import com.mfexpress.rent.deliver.api.ServeServiceI;
import com.mfexpress.rent.deliver.dto.data.serve.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/deliver/v3/serve")
@Api(tags = "api--交付--1.4租赁服务单", value = "ServeController")
@ApiSort(1)
public class ServeController {

    @Resource
    private ServeServiceI serveServiceI;


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

}
