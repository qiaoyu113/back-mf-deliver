package com.mfexpress.rent.deliver.web;

import com.mfexpress.component.constants.CommonConstants;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.token.TokenTools;
import com.mfexpress.rent.deliver.api.ServeServiceI;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAllLeaseTermAmountVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeLeaseTermAmountQry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("serveWebController")
@RequestMapping("/api/deliver/v3/serve/web")
@Api(tags = "api--交付--web--1.0租赁服务单", value = "ServeController")
public class ServeController {

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

}
