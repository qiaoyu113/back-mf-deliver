package com.mfexpress.rent.deliver.mobile;


import com.alibaba.fastjson.JSON;
import com.mfexpress.component.starter.utils.MqTools;
import com.mfexpress.rent.deliver.api.SyncServiceI;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAddDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/deliver/v3/sync")
public class SyncController {

    @Resource
    private MqTools mqTools;
    @Resource
    private SyncServiceI syncServiceI;

    @RequestMapping("/testSend")
    public void testSend(@RequestBody ServeAddDTO serveAddDTO) {
        mqTools.send("dev2m1_event", "order_payment_finish", "", JSON.toJSONString(serveAddDTO));

    }

    @RequestMapping("/execAll")
    public void execAll() {
        syncServiceI.execAll();
    }



}
