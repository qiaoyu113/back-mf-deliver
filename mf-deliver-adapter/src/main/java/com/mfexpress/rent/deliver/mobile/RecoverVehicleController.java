package com.mfexpress.rent.deliver.mobile;

import com.mfexpress.component.constants.CommonConstants;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.TokenTools;
import com.mfexpress.rent.deliver.api.RecoverVehicleServiceI;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/deliver/v3/recovervehicle")
@Api(tags = "api--交付--1.4收车", value = "RecoverVehicleController")
@ApiSort(4)
@Slf4j
public class RecoverVehicleController {

    @Resource
    private RecoverVehicleServiceI recoverVehicleServiceI;


    @PostMapping("/getRecoverVehicleListVO")
    @ApiOperation("申请收车页选择车辆列表")
    @PrintParam
    public Result<List<RecoverApplyVO>> getRecoverVehicleListVO(@RequestBody RecoverApplyQryCmd recoverApplyQryCmd,
                                                                @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            return Result.getInstance((List<RecoverApplyVO>) null).fail(-1, "没有权限");
        }
        return Result.getInstance(recoverVehicleServiceI.getRecoverVehicleListVO(recoverApplyQryCmd, tokenInfo)).success();

    }

    @PostMapping("/applyRecover")
    @ApiOperation("申请收车提交")
    @PrintParam
    public Result<String> applyRecover(@RequestBody RecoverApplyListCmd recoverApplyListCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        // 收车单创建 对应交付单状态更新为收车中
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            //提示失败结果
            return Result.getInstance((String) null).fail(-1, "没有权限");
        }
        recoverApplyListCmd.setCarServiceId(tokenInfo.getId());
        return Result.getInstance(recoverVehicleServiceI.applyRecover(recoverApplyListCmd)).success();
    }


    @PostMapping("/cancelRecover")
    @ApiOperation("取消收车")
    @PrintParam
    public Result<String> cancelRecover(@RequestBody RecoverCancelCmd recoverCancelCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        // 收车单创建 对应交付单状态更新为收车中
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            //提示失败结果
            return Result.getInstance((String) null).fail(-1, "没有权限");
        }
        //交付单状态修改为已发车 将收车单设为失效
        recoverCancelCmd.setCarServiceId(tokenInfo.getId());
        return Result.getInstance(recoverVehicleServiceI.cancelRecover(recoverCancelCmd)).success();

    }

    @PostMapping("/getRecoverListVO")
    @ApiOperation("收车申请列表")
    @PrintParam
    public Result<RecoverTaskListVO> getRecoverListVO(@RequestBody RecoverQryListCmd recoverQryListCmd,
                                                      @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        // 查询es收车中或已收车数据
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            return Result.getInstance((RecoverTaskListVO) null).fail(-1, "没有权限");
        }
        return Result.getInstance(recoverVehicleServiceI.getRecoverListVO(recoverQryListCmd, tokenInfo)).success();
    }

    @PostMapping("/getRecoverTaskListVO")
    @ApiOperation("收车任务列表")
    @PrintParam
    public Result<RecoverTaskListVO> getRecoverTaskListVO(@RequestBody RecoverQryListCmd recoverQryListCmd,
                                                          @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        // 查询es收车中或已收车数据
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            return Result.getInstance((RecoverTaskListVO) null).fail(-1, "没有权限");
        }
        return Result.getInstance(recoverVehicleServiceI.getRecoverListVO(recoverQryListCmd, tokenInfo)).success();
    }


    @PostMapping("/toCheck")
    @ApiOperation(value = "收车验车")
    @PrintParam
    public Result<String> toCheck(@RequestBody RecoverVechicleCmd recoverVechicleCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {

        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            //提示失败结果
            return Result.getInstance((String) null).fail(-1, "没有权限");
        }
        recoverVechicleCmd.setCarServiceId(tokenInfo.getId());
        //交付单更新待验车状态 完善收车单还车人合照信息
        return Result.getInstance(recoverVehicleServiceI.toCheck(recoverVechicleCmd)).success();

    }

    @PostMapping("/toBackInsure")
    @ApiOperation(value = "收车退保")
    @PrintParam
    public Result<String> toBackInsure(@RequestBody RecoverBackInsureCmd recoverBackInsureCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        long start = System.currentTimeMillis();
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            //提示失败结果
            return Result.getInstance((String) null).fail(-1, "没有权限");
        }
        //交付单更新保险状态  更新车辆保险状态
        recoverBackInsureCmd.setCarServiceId(tokenInfo.getId());
        String s = recoverVehicleServiceI.toBackInsure(recoverBackInsureCmd);
        long end = System.currentTimeMillis();
        log.info("toBackInsure:" + recoverBackInsureCmd.getServeNoList().get(0) + "，用时" + (end - start));
        return Result.getInstance(s).success();
    }

    @PostMapping("/toDeduction")
    @ApiOperation(value = "收车处理违章")
    @PrintParam
    public Result<String> toDeduction(@RequestBody RecoverDeductionCmd recoverDeductionCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {

        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            //提示失败结果
            return Result.getInstance((String) null).fail(-1, "没有权限");
        }
        recoverDeductionCmd.setCarServiceId(tokenInfo.getId());
        // 服务单 交付单状态更新已收车  交付单处理违章状态更新  车辆租赁状态更新未租赁

        return Result.getInstance(recoverVehicleServiceI.toDeduction(recoverDeductionCmd));
    }

}
