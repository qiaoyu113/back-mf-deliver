package com.mfexpress.rent.deliver.web;

import com.mfexpress.base.starter.logback.log.PrintParam;
import com.mfexpress.component.constants.CommonConstants;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.token.TokenTools;
import com.mfexpress.rent.deliver.api.ServeServiceI;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAllLeaseTermAmountVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeLeaseTermAmountQry;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCheckCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCmd;
import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeAdjustVO;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.TerminationServiceCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController("serveWebController")
@RequestMapping("/api/deliver/v3/serve/web")
@Api(tags = "api--交付--web--1.0租赁服务单", value = "ServeController")
public class ServeWebController {

    @Resource
    private ServeServiceI serveServiceI;

    @ApiOperation("服务单全部租期费用列表查询")
    @PostMapping("/getServeLeaseTermAmountVOList")
    @PrintParam
    public Result<PagePagination<ServeAllLeaseTermAmountVO>> getServeLeaseTermAmountVOList(@RequestBody @Validated ServeLeaseTermAmountQry qry, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (null == tokenInfo || null == tokenInfo.getOfficeId() || null == tokenInfo.getId()) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(serveServiceI.getServeLeaseTermAmountVOList(qry, tokenInfo)).success();
    }

    @ApiOperation("服务单全部租期费用列表导出")
    @PostMapping("/exportServeLeaseTermAmount")
    @PrintParam
    public Result<Integer> exportServeLeaseTermAmount(@RequestBody @Validated ServeLeaseTermAmountQry qry, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (null == tokenInfo || null == tokenInfo.getOfficeId() || null == tokenInfo.getId()) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(serveServiceI.exportServeLeaseTermAmount(qry, tokenInfo)).success();
    }

    @ApiOperation("服务单全部租期费用列表导出数据")
    @PostMapping("/exportServeLeaseTermAmountData")
    @PrintParam
    public Result<List<Map<String,Object>>> exportServeLeaseTermAmountData(@RequestBody Map<String,Object> map) {
        return Result.getInstance(serveServiceI.exportServeLeaseTermAmountData(map)).success();
    }

    @ApiOperation("重新激活")
    @PostMapping("/reactivate")
    @PrintParam
    public Result<Integer> reactivate(@RequestBody @Validated ReactivateServeCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (null == tokenInfo || null == tokenInfo.getOfficeId() || null == tokenInfo.getId()) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(serveServiceI.reactivate(cmd, tokenInfo)).success();
    }



    /**
     * 判断服务单是否可以进行服务单调整操作接口
     */
    @ApiOperation(value = "服务单调整判断")
    @PostMapping(value = "/serve/adjustment/check")
    @PrintParam
    public Result<ServeAdjustVO> serveAdjustmentCheck(@RequestBody ServeAdjustCheckCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {

        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }

        return Result.getInstance(serveServiceI.serveAdjustCheck(cmd, tokenInfo)).success();
    }

    /**
     * 服务单调整接口
     */
    @ApiOperation(value = "服务单调整")
    @PostMapping(value = "/serve/adjustment")
    @PrintParam
    public Result<Integer> serveAdjustment(@RequestBody ServeAdjustCmd cmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {

        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }

        serveServiceI.serveAdjust(cmd, tokenInfo);

        return Result.getInstance(0).success();
    }


    @ApiOperation(value = "终止服务 v1.13")
    @PostMapping(value = "/serve/terminationService")
    @PrintParam
    public Result<Boolean> terminationService(@RequestBody TerminationServiceCmd terminationServiceCmd, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {

        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (tokenInfo == null) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }

        return Result.getInstance(serveServiceI.terminationService(terminationServiceCmd, tokenInfo)).success();
    }




}
