package com.mfexpress.rent.deliver.domainapi.proxy.backmarket;

import com.hx.backmarket.maintain.data.cmd.maintainapply.*;
import com.hx.backmarket.maintain.data.dto.maintainapply.MaintainApplyDTO;
import com.hx.backmarket.maintain.data.dto.maintainapply.OperationRecordDTO;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "backmarket-maintain", path = "/domain/maintain/v3/apply", url = "${gateway.backmarket}")
public interface BackmarketMaintainApplyAggregateRootApi {

    @PostMapping("/create")
    Result<Long> create(@RequestBody @Validated CreateMaintainApplyCmd cmd);

    @PostMapping("/edit")
    Result<Integer> edit(@RequestBody @Validated EditMaintainApplyCmd cmd);

    @PostMapping("/cancel")
    Result<Integer> cancel(@RequestBody @Validated CancelMaintainApplyCmd cmd);

    @PostMapping("/reject")
    Result<Integer> reject(@RequestBody @Validated RejectMaintainApplyCmd cmd);

    @PostMapping("/agree")
    Result<Integer> agree(@RequestBody @Validated AgreeMaintainApplyCmd cmd);

    @PostMapping("/getOneByApplyId")
    Result<MaintainApplyDTO> getOneByApplyId(@RequestParam("applyId") Long applyId);

    @PostMapping("/getOneByApplyCode")
    Result<MaintainApplyDTO> getOneByApplyCode(@RequestParam("applyCode") String applyCode);

    @PostMapping("/getListByApplyIds")
    Result<List<MaintainApplyDTO>> getListByApplyIds(@RequestBody @Validated MaintainApplyIdsQry qry);

    @PostMapping("/getOperationRecordListBySourceTypeAndSourceId")
    Result<List<OperationRecordDTO>> getOperationRecordListBySourceTypeAndSourceId(@RequestParam("sourceType") Integer sourceType, @RequestParam("sourceId") Long sourceId);

    @PostMapping("/list")
    Result<PagePagination<MaintainApplyDTO>> list(@RequestBody MaintainApplyListQry qry);

}
