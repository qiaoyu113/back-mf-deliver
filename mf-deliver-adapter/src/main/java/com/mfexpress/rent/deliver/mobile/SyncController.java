package com.mfexpress.rent.deliver.mobile;


import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/deliver/v3/sync")
public class SyncController {

    @Resource(name = "serveSyncServiceImpl")
    private EsSyncHandlerI serveSyncServiceI;

    @Resource(name = "deliverSyncServiceImpl")
    private EsSyncHandlerI deliverSyncServiceI;

    @GetMapping("/execServeAll")
    @ApiOperation("同步全部服务单")
    public Result<Boolean> execServeAll(@RequestParam("indexVersionName") String indexVersionName) {
        boolean isSuccess = serveSyncServiceI.execAll(indexVersionName);
        return isSuccess ?Result.getInstance(true).success():Result.getInstance(false).fail(ResultErrorEnum.SERRVER_ERROR.getCode(), "全量同步可能全部失败或部分失败，详情请查看日志");
    }

    @GetMapping("/execServeSingle")
    @ApiOperation("同步单条服务单数据")
    public Result<Boolean> execServeSingle(@RequestParam("serveNo") String serveNo) {
        Map<String, String> data = new HashMap<>();
        data.put("serve_no", serveNo);
        boolean isSuccess = serveSyncServiceI.execOne(data);
        return isSuccess ?Result.getInstance(true).success():Result.getInstance(false).fail(ResultErrorEnum.SERRVER_ERROR.getCode(), "全量同步可能全部失败或部分失败，详情请查看日志");
    }

    @GetMapping("/switchServeAlias")
    @ApiOperation("切换服务单索引的别名")
    public Result<Boolean> switchServeAlias(@RequestParam("alias") String alias, @RequestParam("indexVersion") String indexVersionName){
        return Result.getInstance(serveSyncServiceI.switchAliasIndex(alias, indexVersionName)).success();
    }

    @GetMapping("/execDeliverAll")
    @ApiOperation("同步全部交付单")
    public Result<Boolean> execDeliverAll(@RequestParam("indexVersionName") String indexVersionName) {
        boolean isSuccess = deliverSyncServiceI.execAll(indexVersionName);
        return isSuccess ?Result.getInstance(true).success():Result.getInstance(false).fail(ResultErrorEnum.SERRVER_ERROR.getCode(), "全量同步可能全部失败或部分失败，详情请查看日志");
    }

    @GetMapping("/switchDeliverAlias")
    @ApiOperation("切换交付单索引的别名")
    public Result<Boolean> switchDeliverAlias(@RequestParam("alias") String alias, @RequestParam("indexVersion") String indexVersionName){
        return Result.getInstance(deliverSyncServiceI.switchAliasIndex(alias, indexVersionName)).success();
    }



}
