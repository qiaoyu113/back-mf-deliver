package com.mfexpress.rent.deliver.domainapi;

import javax.annotation.Resource;

import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.MfDeliveryApplication;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeCancelCmd;
import io.swagger.models.auth.In;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(Runner.class)
@SpringBootTest(classes = MfDeliveryApplication.class)
class ServeAggregateRootApiTest {

    @Resource
    ServeAggregateRootApi serveAggregateRootApi;

    @Test
    void cancelServe() {
        ServeCancelCmd cmd = new ServeCancelCmd();
        cmd.setServeNo("FWD2022061400038");
        cmd.setOperatorId(-999);

        Result<Integer> result = serveAggregateRootApi.cancelServe(cmd);

        ResultValidUtils.checkResultException(result);

    }
}