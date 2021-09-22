package com.mfexpress.rent.deliver.mobile;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.api.RecoverVehicleServiceI;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Result<List<RecoverApplyVO>> getRecoverVehicleListVO(@RequestBody RecoverApplyQryCmd recoverApplyQryCmd) {

        return Result.getInstance(recoverVehicleServiceI.getRecoverVehicleListVO(recoverApplyQryCmd)).success();

    }

    @PostMapping("/applyRecover")
    @ApiOperation("申请收车提交")
    public Result<String> applyRecover(@RequestBody RecoverApplyListCmd recoverApplyListCmd) {
        // 收车单创建 对应交付单状态更新为收车中

        return Result.getInstance(recoverVehicleServiceI.applyRecover(recoverApplyListCmd)).success();
    }


    @PostMapping("/cancelRecover")
    @ApiOperation("取消收车")
    public Result<String> cancelRecover(@RequestBody RecoverCancelCmd recoverCancelCmd) {

        //交付单状态修改为已发车 将收车单设为失效

        return Result.getInstance(recoverVehicleServiceI.cancelRecover(recoverCancelCmd)).success();

    }

    @PostMapping("/getRecoverListVO")
    @ApiOperation("收车申请列表")
    public Result<RecoverTaskListVO> getRecoverListVO(@RequestBody RecoverQryListCmd recoverQryListCmd) {
        // 查询es收车中或已收车数据
        return Result.getInstance(recoverVehicleServiceI.getRecoverListVO(recoverQryListCmd)).success();
    }

    @PostMapping("/getRecoverTaskListVO")
    @ApiOperation("收车任务列表")
    public Result<RecoverTaskListVO> getRecoverTaskListVO(@RequestBody RecoverQryListCmd recoverQryListCmd) {
        // 查询es收车中或已收车数据
        return Result.getInstance(recoverVehicleServiceI.getRecoverListVO(recoverQryListCmd)).success();
    }


    @PostMapping("/toCheck")
    @ApiOperation(value = "收车验车")
    public Result<String> toCheck(@RequestBody RecoverVechicleCmd recoverVechicleCmd) {
        //交付单更新待验车状态 完善收车单还车人合照信息
        return Result.getInstance(recoverVehicleServiceI.toCheck(recoverVechicleCmd)).success();

    }

    @PostMapping("/toBackInsure")
    @ApiOperation(value = "收车退保")
    public Result<String> toBackInsure(@RequestBody RecoverBackInsureCmd recoverBackInsureCmd) {

        //交付单更新保险状态  更新车辆保险状态

        return Result.getInstance(recoverVehicleServiceI.toBackInsure(recoverBackInsureCmd)).success();
    }

    @PostMapping("/toDeduction")
    @ApiOperation(value = "收车处理违章")
    public Result<String> toDeduction(@RequestBody RecoverDeductionCmd recoverDeductionCmd) {

        // 服务单 交付单状态更新已收车  交付单处理违章状态更新  车辆租赁状态更新未租赁

        return Result.getInstance(recoverVehicleServiceI.toDeduction(recoverDeductionCmd));
    }

}
