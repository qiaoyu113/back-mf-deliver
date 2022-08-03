package com.mfexpress.rent.deliver.web;

import com.mfexpress.component.constants.CommonConstants;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.base.starter.logback.log.PrintParam;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.token.TokenTools;
import com.mfexpress.rent.deliver.api.DeliverServiceI;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverEachLeaseTermAmountVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController("deliverWebController")
@RequestMapping("/api/deliver/v3/deliver/web")
@Api(tags = "api--交付--web--1.0租赁交付单", value = "DeliverController")
public class DeliverWebController {

    @Resource
    private DeliverServiceI deliverServiceI;

    @ApiOperation("交付单各租期费用列表查询")
    @PostMapping("/getDeliverLeaseTermAmountVOList")
    @PrintParam
    public Result<PagePagination<DeliverEachLeaseTermAmountVO>> getDeliverLeaseTermAmountVOList(@RequestBody @Validated ServeQryCmd qry) {
        return Result.getInstance(deliverServiceI.getDeliverLeaseTermAmountVOList(qry)).success();
    }

    @ApiOperation("导出交付单各租期费用")
    @PostMapping("/exportDeliverLeaseTermAmount")
    @PrintParam
    public Result<Integer> exportDeliverLeaseTermAmount(@RequestBody @Validated ServeQryCmd qry, @RequestHeader(CommonConstants.TOKEN_HEADER) String jwt) {
        TokenInfo tokenInfo = TokenTools.parseToken(jwt, TokenInfo.class);
        if (null == tokenInfo || null == tokenInfo.getOfficeId() || null == tokenInfo.getId()) {
            throw new CommonException(ResultErrorEnum.LOGIN_OVERDUE.getCode(), ResultErrorEnum.LOGIN_OVERDUE.getName());
        }
        return Result.getInstance(deliverServiceI.exportDeliverLeaseTermAmount(qry, tokenInfo)).success();
    }

    @ApiOperation("查询交付单各租期费用")
    @PostMapping("/exportDeliverLeaseTermAmountData")
    @PrintParam
    public Result<List<Map<String,Object>>> exportDeliverLeaseTermAmountData(@RequestBody Map<String, Object> map) {
        return Result.getInstance(deliverServiceI.exportDeliverLeaseTermAmountData(map)).success();
    }
}
