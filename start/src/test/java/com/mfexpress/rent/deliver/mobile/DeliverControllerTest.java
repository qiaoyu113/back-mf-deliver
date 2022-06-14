package com.mfexpress.rent.deliver.mobile;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.MfDeliveryApplication;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCheckCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverInsureCmd;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MfDeliveryApplication.class)
class DeliverControllerTest {

    @Resource
    DeliverController controller;

    String jwt = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySW5mbyI6ImV5SmhZMk52ZFc1MFRtOXVSWGh3YVhKbFpDSTZkSEoxWlN3aVlXTmpiM1Z1ZEU1dmJreHZZMnRsWkNJNmRISjFaU3dpWVhWMGFHVnVkR2xqWVhSbFpDSTZkSEoxWlN3aVluVlVlWEJsSWpvd0xDSmphWFI1U1dRaU9qRXNJbU52Y25CVmMyVnlTV1FpT2lJaUxDSmpjbVZoZEdWRVlYUmxJam94TmpBNU1qRXlNelU1TURBd0xDSmpjbVZoZEdWSlpDSTZNQ3dpWTNKbFpHVnVkR2xoYkhOT2IyNUZlSEJwY21Wa0lqcDBjblZsTENKa1pXeEdiR0ZuSWpvd0xDSmtkWFI1U1dRaU9qazVPVGtzSW1WdVlXSnNaV1FpT25SeWRXVXNJbWxrSWpvdE9UazVMQ0p0YjJKcGJHVWlPaUl4TXpNd01EQXdNREF3TUNJc0ltNXBZMnRPWVcxbElqb2k1N083NTd1Zklpd2liMlptYVdObFNXUWlPakVzSW5CaGMzTjNiM0prSWpvaUlpd2ljbTlzWlVsa0lqb3dMQ0p6WlhSMGFXNW5SbXhoWnlJNk1Td2ljM1JoZEhWeklqb3hMQ0owYjJ0bGJrVjRjR2x5WldRaU9qRTJOakV6TURjek1UazBOVFFzSW5SNWNHVWlPakFzSW5Wd1pHRjBaVVJoZEdVaU9qRTJOVEl3T0RNNU16QXdNREFzSW5Wd1pHRjBaVWxrSWpvd0xDSjFjMlZ5Ym1GdFpTSTZJakV6TXpBd01EQXdNREF3SW4wPSIsInN1YiI6IjEzMzAwMDAwMDAwIiwiZXhwIjoxNjYxMzA3MzE5fQ.W8kKqkMGychXeCUUyI4zYuMkUo9cVF7caezL2zeZCmxWedu6YnccGSq7MP1TlUBUkGgcOAadl38w0XkKuHmajg";

    // TODO 服务单号
    String serveNo = "";

    @Test
    void toCheck() {

        DeliverCheckCmd cmd = new DeliverCheckCmd();
        cmd.setServeNo(serveNo);
        controller.toCheck(cmd, jwt);
    }

    @Test
    void toInsure() {
        /**
         *
         * {
         *   "startInsureDate": 1655136000000,
         *   "endInsureDate": 1655222400000,
         *   "serveNoList": [
         *     "FWD2022061400029"
         *   ],
         *   "carIdList": [
         *     6489
         *   ]
         * }
         */
        DeliverInsureCmd cmd = new DeliverInsureCmd();

        cmd.setServeNoList(Collections.singletonList("FWD2022061400029"));
        cmd.setCarIdList(Collections.singletonList(6489));
        cmd.setStartInsureDate(new Date());
        cmd.setEndInsureDate(FormatUtil.addDays(new Date(), 2));

        Result<String> result = controller.toInsure(cmd, jwt);
        log.info("result---->{}", result);
    }
}