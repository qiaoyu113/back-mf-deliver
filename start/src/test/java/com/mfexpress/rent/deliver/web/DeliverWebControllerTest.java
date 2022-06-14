package com.mfexpress.rent.deliver.web;

import java.util.List;

import javax.annotation.Resource;

import com.mfexpress.billing.rentcharge.api.FeeAggregeateRootApi;
import com.mfexpress.billing.rentcharge.dto.data.fee.ServeLeaseTermInfoDTO;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.MfDeliveryApplication;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverEachLeaseTermAmountVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeQryCmd;
import com.mfexpress.transportation.customer.dto.entity.Customer;
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

    @Resource
    FeeAggregeateRootApi feeAggregeateRootApi;

    @Test
    void getDeliverLeaseTermAmountVOList() {

        ServeQryCmd qryCmd = new ServeQryCmd();
        qryCmd.setServeNo("FWD2022060800041");
        Result<PagePagination<DeliverEachLeaseTermAmountVO>> result = controller.getDeliverLeaseTermAmountVOList(qryCmd);

        log.info("result---->{}", result);


//        Result<List<ServeLeaseTermInfoDTO>> serveLeaseTermInfoDTOListResult = feeAggregeateRootApi.getServeLeaseTermInfoByServeNo(qryCmd.getServeNo());

//        log.info("serveLeaseTermInfoDTOListResult---->{}", serveLeaseTermInfoDTOListResult);
    }

    @Test
    void exportDeliverLeaseTermAmount() {
    }

    @Test
    void exportDeliverLeaseTermAmountData() {
    }
}