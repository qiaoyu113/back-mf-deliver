package com.mfexpress.rent.deliver.gateway;

import javax.annotation.Resource;

import com.mfexpress.rent.deliver.MfDeliveryApplication;
import com.mfexpress.rent.deliver.entity.ServeEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(classes = MfDeliveryApplication.class)
@RunWith(SpringRunner.class)
class ServeGatewayTest {

    @Resource
    ServeGateway serveGateway;

    @Test
    void updateServeByServeNo() {
    }

    @Test
    void updateServeByServeNoList() {
    }

    @Test
    void getServeByServeNo() {

        ServeEntity serve = serveGateway.getServeByServeNo("FWD2022061300034");

        log.info("serve---->{}", serve);
    }

    @Test
    void addServeList() {
    }

    @Test
    void getServePreselectedByOrderId() {
    }

    @Test
    void getServeNoListAll() {
    }

    @Test
    void getServeByStatus() {
    }

    @Test
    void getCycleServe() {
    }

    @Test
    void getServeByServeNoList() {
    }

    @Test
    void getServeListByOrderIds() {
    }

    @Test
    void getCountByQry() {
    }

    @Test
    void getPageServeByQry() {
    }

    @Test
    void batchUpdate() {
    }

    @Test
    void getServeByCustomerIdDeliver() {
    }

    @Test
    void getServeByCustomerIdRecover() {
    }

    @Test
    void getServeNoListByPage() {
    }

    @Test
    void getServeDepositByServeNo() {
    }

    @Test
    void pageServeDeposit() {
    }

    @Test
    void getReplaceNumByCustomerIds() {
    }

    @Test
    void getRentingServeNumByCustomerId() {
    }
}