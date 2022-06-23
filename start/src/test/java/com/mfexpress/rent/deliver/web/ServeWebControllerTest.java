package com.mfexpress.rent.deliver.web;

import javax.annotation.Resource;

import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.MfDeliveryApplication;
import com.mfexpress.rent.deliver.constant.DepositPayTypeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.ServeAllLeaseTermAmountVO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeLeaseTermAmountQry;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCheckCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCmd;
import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeAdjustVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MfDeliveryApplication.class)
class ServeWebControllerTest {

    @Resource
    ServeWebController serveWebController;

    String jwt = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySW5mbyI6ImV5SmhZMk52ZFc1MFRtOXVSWGh3YVhKbFpDSTZkSEoxWlN3aVlXTmpiM1Z1ZEU1dmJreHZZMnRsWkNJNmRISjFaU3dpWVhWMGFHVnVkR2xqWVhSbFpDSTZkSEoxWlN3aVluVlVlWEJsSWpvd0xDSmphWFI1U1dRaU9qRXNJbU52Y25CVmMyVnlTV1FpT2lJaUxDSmpjbVZoZEdWRVlYUmxJam94TmpBNU1qRXlNelU1TURBd0xDSmpjbVZoZEdWSlpDSTZNQ3dpWTNKbFpHVnVkR2xoYkhOT2IyNUZlSEJwY21Wa0lqcDBjblZsTENKa1pXeEdiR0ZuSWpvd0xDSmtkWFI1U1dRaU9qazVPVGtzSW1WdVlXSnNaV1FpT25SeWRXVXNJbWxrSWpvdE9UazVMQ0p0YjJKcGJHVWlPaUl4TXpNd01EQXdNREF3TUNJc0ltNXBZMnRPWVcxbElqb2k1N083NTd1Zklpd2liMlptYVdObFNXUWlPakVzSW5CaGMzTjNiM0prSWpvaUlpd2ljbTlzWlVsa0lqb3dMQ0p6WlhSMGFXNW5SbXhoWnlJNk1Td2ljM1JoZEhWeklqb3hMQ0owYjJ0bGJrVjRjR2x5WldRaU9qRTJOakV6TURjek1UazBOVFFzSW5SNWNHVWlPakFzSW5Wd1pHRjBaVVJoZEdVaU9qRTJOVEl3T0RNNU16QXdNREFzSW5Wd1pHRjBaVWxrSWpvd0xDSjFjMlZ5Ym1GdFpTSTZJakV6TXpBd01EQXdNREF3SW4wPSIsInN1YiI6IjEzMzAwMDAwMDAwIiwiZXhwIjoxNjYxMzA3MzE5fQ.W8kKqkMGychXeCUUyI4zYuMkUo9cVF7caezL2zeZCmxWedu6YnccGSq7MP1TlUBUkGgcOAadl38w0XkKuHmajg";

    // TODO 服务单号
    String serveNo = "FWD2021110100008";

    @Resource
    ServeAggregateRootApi serveAggregateRootApi;

    @Test
    void getServeLeaseTermAmountVOList() {

        ServeLeaseTermAmountQry qry = new ServeLeaseTermAmountQry();
//        qry.setCarId(5616);


        Result<PagePagination<ServeAllLeaseTermAmountVO>> result = serveWebController.getServeLeaseTermAmountVOList(qry, jwt);

        log.info("result----{}", result);
    }

    @Test
    void exportServeLeaseTermAmount() {

    }

    @Test
    void exportServeLeaseTermAmountData() {
    }

    @Test
    void reactivate() {
    }

    @Test
    void serveAdjustmentCheck() {

        ServeAdjustCheckCmd cmd = new ServeAdjustCheckCmd();
        cmd.setServeNo(serveNo);
        Result<ServeAdjustVO> result = serveWebController.serveAdjustmentCheck(cmd, jwt);
        log.info("result----->{}", result);
    }

    @Test
    void serveAdjustment() {

        ServeAdjustCmd cmd = new ServeAdjustCmd();
        cmd.setServeNo(serveNo);
        cmd.setDepositPayType(DepositPayTypeEnum.ACCOUNT_DEPOSIT_UNLOCK_PAY.getCode());

        serveWebController.serveAdjustment(cmd, jwt);
    }
}