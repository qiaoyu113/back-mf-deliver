package com.mfexpress.rent.deliver.web;

import javax.annotation.Resource;

import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.MfDeliveryApplication;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverEachLeaseTermAmountVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryCmd;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(classes = MfDeliveryApplication.class)
@RunWith(SpringRunner.class)
class DeliverWebControllerTest {

    @Resource
    DeliverWebController controller;

    @Test
    void getDeliverLeaseTermAmountVOList() {

        ServeQryCmd qryCmd = new ServeQryCmd();
        qryCmd.setServeNo("FWD2022061300034");
        Result<PagePagination<DeliverEachLeaseTermAmountVO>> result = controller.getDeliverLeaseTermAmountVOList(qryCmd);

        log.info("result---->{}", result);
    }

    @Test
    void exportDeliverLeaseTermAmount() {
    }

    @Test
    void exportDeliverLeaseTermAmountData() {
    }
}