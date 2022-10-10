package com.mfexpress.rent.deliver.mobile;

import com.mfexpress.component.constants.CommonConstants;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.base.starter.logback.log.PrintParam;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.token.TokenTools;
import com.mfexpress.rent.deliver.api.RecoverVehicleServiceI;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.*;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.vo.SurrenderApplyVO;
import com.mfexpress.transportation.customer.dto.entity.vo.LinkmanVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/deliver/v3/recovervehicle")
@Api(tags = "api--交付--1.4收车", value = "RecoverVehicleController")
@ApiSort(4)
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
            return Result.getInstance((List<RecoverApplyVO>) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
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
            return Result.getInstance((String) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
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
            return Result.getInstance((String) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        //交付单状态修改为已发车 将收车单设为失效
        recoverCancelCmd.setCarServiceId(tokenInfo.getId());
        return Result.getInstance(recoverVehicleServiceI.cancelRecover(recoverCancelCmd)).success();

    }

    @PostMapping("/cancelRecoverByDeliver")
    @ApiOperation("取消收车")
    @PrintParam
    public Result<Integer> cancelRecoverByDeliver(@RequestBody @Validated RecoverCancelByDeliverCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        // 收车单创建 对应交付单状态更新为收车中
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            throw new CommonException(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        //交付单状态修改为已发车 将收车单设为失效
        return Result.getInstance(recoverVehicleServiceI.cancelRecoverByDeliver(cmd, tokenInfo)).success();

    }

    @PostMapping("/getRecoverListVO")
    @ApiOperation("收车申请列表")
    @PrintParam
    public Result<RecoverTaskListVO> getRecoverListVO(@RequestBody RecoverQryListCmd recoverQryListCmd,
                                                      @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        // 查询es收车中或已收车数据
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            return Result.getInstance((RecoverTaskListVO) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
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
            return Result.getInstance((RecoverTaskListVO) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        return Result.getInstance(recoverVehicleServiceI.getRecoverListVO(recoverQryListCmd, tokenInfo)).success();
    }


    @PostMapping("/whetherToCheck")
    @ApiOperation(value = "是否可收车验车")
    @PrintParam
    @Deprecated
    public Result<Boolean> whetherToCheck(@RequestBody RecoverVechicleCmd recoverVechicleCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {

        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            //提示失败结果
            return Result.getInstance((Boolean) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        recoverVechicleCmd.setCarServiceId(tokenInfo.getId());
        //交付单更新待验车状态 完善收车单还车人合照信息
        return Result.getInstance(recoverVehicleServiceI.whetherToCheck(recoverVechicleCmd)).success();

    }

    @PostMapping("/toBackInsure")
    @ApiOperation(value = "收车退保")
    @PrintParam
    public Result<String> toBackInsure(@RequestBody RecoverBackInsureCmd recoverBackInsureCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            //提示失败结果
            return Result.getInstance((String) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        //交付单更新保险状态  更新车辆保险状态
        recoverBackInsureCmd.setCarServiceId(tokenInfo.getId());
        return Result.getInstance(recoverVehicleServiceI.toBackInsure(recoverBackInsureCmd)).success();
    }

    @PostMapping("/toBackInsureByDeliver")
    @ApiOperation(value = "通过交付单进行收车退保操作")
    @PrintParam
    public Result<Integer> toBackInsure(@RequestBody @Validated RecoverBackInsureByDeliverCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (null == tokenInfo || null == tokenInfo.getOfficeId() || null == tokenInfo.getId()) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(recoverVehicleServiceI.toBackInsureByDeliver(cmd, tokenInfo)).success();
    }

    @PostMapping("/toDeduction")
    @ApiOperation(value = "收车处理违章")
    @PrintParam
    public Result<String> toDeduction(@RequestBody @Validated RecoverDeductionCmd recoverDeductionCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            //提示失败结果
            return Result.getInstance((String) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        recoverDeductionCmd.setCarServiceId(tokenInfo.getId());
        // 服务单 交付单状态更新已收车  交付单处理违章状态更新  车辆租赁状态更新未租赁
        return Result.getInstance(recoverVehicleServiceI.toDeduction(recoverDeductionCmd)).success();
    }

    @PostMapping("/toDeductionByDeliver")
    @ApiOperation(value = "通过交付单进行收车处理违章操作")
    @PrintParam
    public Result<Integer> toDeductionByDeliver(@RequestBody @Validated RecoverDeductionByDeliverCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            throw new CommonException(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        return Result.getInstance(recoverVehicleServiceI.toDeductionByDeliver(cmd, tokenInfo)).success();
    }

    // ---------------------luzheng add start----------------------------

    @PostMapping("/cacheCheckInfo")
    @ApiOperation(value = "收车验车信息暂存")
    @PrintParam
    public Result<String> cacheCheckInfo(@RequestBody RecoverVechicleCmd recoverVechicleCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            return Result.getInstance((String) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        // 以serverNo为key
        recoverVechicleCmd.setCarServiceId(tokenInfo.getId());
        return Result.getInstance(recoverVehicleServiceI.cacheCheckInfo(recoverVechicleCmd)).success();
    }

    // 获取收车验车信息暂存
    @PostMapping("/getCachedCheckInfo")
    @ApiOperation(value = "获取暂存的收车验车信息")
    @PrintParam
    public Result<RecoverVehicleVO> getCachedCheckInfo(@RequestBody RecoverVechicleCmd recoverVechicleCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            return Result.getInstance((RecoverVehicleVO) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        recoverVechicleCmd.setCarServiceId(tokenInfo.getId());
        return Result.getInstance(recoverVehicleServiceI.getCachedCheckInfo(recoverVechicleCmd)).success();
    }

    // 收车申请详情页
    @PostMapping("/getRecoverDetail")
    @ApiOperation(value = "获取收车申请详情信息")
    @PrintParam
    public Result<RecoverDetailVO> getRecoverDetail(@RequestBody @Validated RecoverDetailQryCmd cmd) {
        return Result.getInstance(recoverVehicleServiceI.getRecoverDetail(cmd)).success();
    }

    // ---------------------luzheng add end----------------------------

    // 异常收车        æbˈnɔːml
    @PostMapping("/abnormalRecover")
    @ApiOperation(value = "异常收车")
    @PrintParam
    public Result<Integer> abnormalRecover(@RequestBody @Validated RecoverAbnormalCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            return Result.getInstance((Integer) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        return Result.getInstance(recoverVehicleServiceI.abnormalRecover(cmd, tokenInfo)).success();
    }

    @PostMapping("/getAbnormalRecoverInfo")
    @ApiOperation(value = "获取异常收车信息")
    @PrintParam
    public Result<RecoverAbnormalVO> getRecoverAbnormalInfo(@RequestBody @Validated RecoverAbnormalQry cmd) {
        return Result.getInstance(recoverVehicleServiceI.getRecoverAbnormalInfo(cmd)).success();
    }


    @PostMapping("/getRecoverVehicleDtoByDeliverNo")
    @ApiOperation(value = "根据customerCmd获取收车人信息")
    @PrintParam
    public Result<LinkmanVo> getRecoverVehicleDtoByDeliverNo(@RequestBody @Validated CustomerCmd customerCmd) {
        return Result.getInstance(recoverVehicleServiceI.getRecoverVehicleDtoByDeliverNo(customerCmd.getCustomerId()));
    }

    @PostMapping("/backInsureByDeliver")
    @ApiOperation(value = "通过交付单进行退保操作")
    @PrintParam
    public Result<SurrenderApplyVO> backInsureByDeliver(@RequestBody @Validated RecoverBackInsureByDeliverCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            return Result.getInstance((SurrenderApplyVO) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        return Result.getInstance(recoverVehicleServiceI.backInsureByDeliver(cmd, tokenInfo)).success();
    }

}
