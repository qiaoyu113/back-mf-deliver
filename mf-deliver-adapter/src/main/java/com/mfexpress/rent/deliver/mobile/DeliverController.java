package com.mfexpress.rent.deliver.mobile;

import com.mfexpress.component.constants.CommonConstants;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.base.starter.logback.log.PrintParam;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.token.TokenTools;
import com.mfexpress.rent.deliver.api.DeliverServiceI;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCheckCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverInsureCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverPreselectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverReplaceCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.CancelPreSelectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.DeliverInsureByCustomerCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.DeliverReplaceVehicleCheckCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.InsureApplyQry;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.InsureApplyVO;
import com.mfexpress.rent.deliver.dto.data.deliver.vo.TipVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/deliver/v3/deliver")
@Api(tags = "api--交付--1.4交付单收发车操作", value = "DeliverController")
@ApiSort(2)
public class DeliverController {

    @Resource
    private DeliverServiceI deliverServiceI;


    @PostMapping("/toPreselected")
    @ApiOperation("预选车辆")
    @PrintParam
    public Result<String> toPreselected(@RequestBody @Validated DeliverPreselectedCmd deliverPreselectedCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {

        //组合生成交付单、交付单状态未1发车中 服务单状态更新未已预选
        // 车辆状态若已投保更新交付单投保状态  调用车辆服务 更新为已预选
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            //提示失败结果
            return Result.getInstance((String) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        deliverPreselectedCmd.setCarServiceId(tokenInfo.getId());

        return Result.getInstance(deliverServiceI.toPreselected(deliverPreselectedCmd)).success();
    }

    @PostMapping("/preselectedVehicle")
    @ApiOperation("预选车辆->新版->关联保险逻辑->1.05去除保险逻辑")
    @PrintParam
    public Result<TipVO> preselectedVehicle(@RequestBody @Validated DeliverPreselectedCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            throw new CommonException(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        return Result.getInstance(deliverServiceI.preselectedVehicle(cmd, tokenInfo)).success();
    }

    @PostMapping("/replaceVehicle")
    @ApiOperation("更换车辆->新版->关联保险逻辑->1.05去除保险逻辑")
    @PrintParam
    public Result<TipVO> replaceVehicle(@RequestBody DeliverReplaceCmd deliverReplaceCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            throw new CommonException(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        deliverReplaceCmd.setCarServiceId(tokenInfo.getId());
        return Result.getInstance(deliverServiceI.toReplace(deliverReplaceCmd)).success();
    }

    @PostMapping("/toCheck")
    @ApiOperation("验车")
    @PrintParam
    public Result<String> toCheck(@RequestBody @Validated DeliverCheckCmd deliverCheckCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        // 交付单更新验车状态
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            //提示失败结果
            return Result.getInstance((String) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        deliverCheckCmd.setCarServiceId(tokenInfo.getId());
        return Result.getInstance(deliverServiceI.toCheck(deliverCheckCmd)).success();
    }

    @PostMapping("/toReplace")
    @ApiOperation("更换车辆")
    @PrintParam
    public Result<TipVO> toReplace(@RequestBody DeliverReplaceCmd deliverReplaceCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        // 当前交付单设未失效  新生成收付单 服务单初始化预选状态 调用车辆服务更新为未预选
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            throw new CommonException(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        deliverReplaceCmd.setCarServiceId(tokenInfo.getId());
        return Result.getInstance(deliverServiceI.toReplace(deliverReplaceCmd)).success();

    }

    @PostMapping("/toInsure")
    @ApiOperation("投保")
    @PrintParam
    public Result<String> toInsure(@RequestBody DeliverInsureCmd deliverInsureCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {

        //更新交付单投保状态  调用车辆更新车辆状态为已投保
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            //提示失败结果
            return Result.getInstance((String) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        deliverInsureCmd.setCarServiceId(tokenInfo.getId());
        return Result.getInstance(deliverServiceI.toInsure(deliverInsureCmd)).success();
    }

    /**
     * 新版投保接口
     */
    @PostMapping("/insureByCompany")
    @ApiOperation("由公司投保-投保申请")
    @PrintParam
    public Result<InsureApplyVO> insureByCompany(@RequestBody @Validated DeliverInsureCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            throw new CommonException(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        return Result.getInstance(deliverServiceI.insureByCompany(cmd, tokenInfo)).success();
    }

    @PostMapping("/insureByCustomer")
    @ApiOperation("由客户投保-录入保单信息")
    @PrintParam
    public Result<Integer> insureByCustomer(@RequestBody DeliverInsureByCustomerCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            throw new CommonException(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        return Result.getInstance(deliverServiceI.insureByCustomer(cmd, tokenInfo)).success();
    }

    @PostMapping("/replaceVehicleShowTip")
    @ApiOperation("更换车辆按钮提示信息展示")
    @PrintParam
    public Result<TipVO> replaceVehicleShowTip(@RequestBody DeliverReplaceVehicleCheckCmd cmd) {
        return Result.getInstance(deliverServiceI.replaceVehicleShowTip(cmd)).success();
    }

    @PostMapping("/getInsureInfo")
    @ApiOperation("查看投保状态接口")
    @PrintParam
    public Result<InsureApplyVO> getInsureInfo(@RequestBody InsureApplyQry qry) {
        return Result.getInstance(deliverServiceI.getInsureInfo(qry)).success();
    }

    @PostMapping("/cancelPreSelected")
    @ApiOperation("取消预选")
    @PrintParam
    public Result<TipVO> cancelPreSelected(@RequestBody @Validated CancelPreSelectedCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            throw new CommonException(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
        }
        return Result.getInstance(deliverServiceI.cancelPreSelected(cmd, tokenInfo)).success();
    }

}
