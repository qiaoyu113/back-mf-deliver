package com.mfexpress.rent.deliver.domainapi;

import javax.annotation.Resource;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.MfDeliveryApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MfDeliveryApplication.class)
class RecoverVehicleAggregateRootApiTestre {

    @Resource
    RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Test
    void recovered() {

        Result<Integer> result = recoverVehicleAggregateRootApi.recovered("JFD2022061400026", "2974313593773818215");

        log.info("result---->{}", result);
    }
}