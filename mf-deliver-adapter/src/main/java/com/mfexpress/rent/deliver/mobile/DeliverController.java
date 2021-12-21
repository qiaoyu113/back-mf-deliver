package com.mfexpress.rent.deliver.mobile;

import com.mfexpress.component.constants.CommonConstants;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.TokenTools;
import com.mfexpress.rent.deliver.api.DeliverServiceI;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCheckCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverInsureCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverPreselectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverReplaceCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiSort;
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
    public Result<String> toPreselected(@RequestBody DeliverPreselectedCmd deliverPreselectedCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {

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

    @PostMapping("/toCheck")
    @ApiOperation("验车")
    @PrintParam
    public Result<String> toCheck(@RequestBody DeliverCheckCmd deliverCheckCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
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
    public Result<String> toReplace(@RequestBody DeliverReplaceCmd deliverReplaceCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        // 当前交付单设未失效  新生成收付单 服务单初始化预选状态 调用车辆服务更新为未预选
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            //提示失败结果
            return Result.getInstance((String) null).fail(ResultErrorEnum.AUTH_ERROR.getCode(), ResultErrorEnum.AUTH_ERROR.getName());
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


}
