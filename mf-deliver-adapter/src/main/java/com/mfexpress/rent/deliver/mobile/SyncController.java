package com.mfexpress.rent.deliver.mobile;


import com.mfexpress.rent.deliver.api.SyncServiceI;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/deliver/v3/sync")
public class SyncController {

    @Resource
    private SyncServiceI syncServiceI;


    @RequestMapping("/execAll")
    @ApiOperation("同步")
    public void execAll() {
        syncServiceI.execAll();
    }



}
