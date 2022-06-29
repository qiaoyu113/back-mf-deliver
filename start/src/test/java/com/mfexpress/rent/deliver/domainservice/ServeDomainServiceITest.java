package com.mfexpress.rent.deliver.domainservice;

import javax.annotation.Resource;

import com.mfexpress.rent.deliver.MfDeliveryApplication;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositListDTO;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MfDeliveryApplication.class)
class ServeDomainServiceITest {

    @Resource
    ServeDomainServiceI serveDomainServiceI;

    @Test
    void getPageServeDeposit() {

        serveDomainServiceI.getPageServeDeposit(CustomerDepositListDTO.builder().customerId(1248).build());
    }
}