package com.mfexpress.rent.deliver.web;

import com.mfexpress.component.log.PrintParam;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverEachLeaseTermAmountVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("deliverWebController")
@RequestMapping("/api/deliver/v3/deliver/web")
@Api(tags = "api--交付--web--1.0租赁交付单", value = "DeliverController")
public class DeliverController {

    @ApiOperation("交付单各租期费用列表查询")
    @PostMapping("/getDeliverLeaseTermAmountVOList")
    @PrintParam
    public Result<PagePagination<DeliverEachLeaseTermAmountVO>> getDeliverLeaseTermAmountVOList(@RequestBody @Validated ServeQryCmd qry) {

        return null;
    }

    @ApiOperation("导出交付单各租期费用列表查询")
    @PostMapping("/exportDeliverLeaseTermAmount")
    @PrintParam
    public Result<Integer> exportDeliverLeaseTermAmount(@RequestBody @Validated ServeQryCmd qry) {

        return null;
    }
}
