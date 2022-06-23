package com.mfexpress.rent.deliver.entity.api;

import javax.annotation.Resource;

import com.mfexpress.rent.deliver.MfDeliveryApplication;
import com.mfexpress.rent.deliver.dto.data.serve.CustomerDepositListDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MfDeliveryApplication.class)
class ServeEntityApiTest {

    @Resource
    ServeEntityApi serveEntityApi;

    @Test
    void getServeDepositByQry() {

        serveEntityApi.getServeDepositByQry(CustomerDepositListDTO.builder().customerId(1248).build());
    }
}