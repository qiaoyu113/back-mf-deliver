package com.mfexpress.rent.deliver.mobile;


import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/deliver/v3/sync")
public class SyncController {

    @Resource
    private EsSyncHandlerI syncServiceI;

    @RequestMapping("/execAll")
    @ApiOperation("同步")
    public Result<Boolean> execAll() {
        boolean isSuccess = syncServiceI.execAll();
        return isSuccess ?Result.getInstance(true).success():Result.getInstance(false).fail(ResultErrorEnum.SERRVER_ERROR.getCode(), "全量同步可能全部失败或部分失败，详情请查看日志");
    }

}
