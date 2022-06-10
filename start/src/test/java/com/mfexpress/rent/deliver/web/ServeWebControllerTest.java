package com.mfexpress.rent.deliver.web;

import java.math.BigDecimal;

import javax.annotation.Resource;

import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.MfDeliveryApplication;
import com.mfexpress.rent.deliver.constant.ReplaceVehicleDepositPayTypeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.serve.ServeReplaceVehicleAddDTO;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCheckCmd;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustCmd;
import com.mfexpress.rent.deliver.dto.data.serve.vo.ServeAdjustRecordVo;
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
    String serveNo = "FWD2022030700027";

    @Resource
    ServeAggregateRootApi serveAggregateRootApi;

    @Test
    void getServeLeaseTermAmountVOList() {

        ServeReplaceVehicleAddDTO dto = new ServeReplaceVehicleAddDTO();

        dto.setRentRatio(new BigDecimal(1));
        dto.setRent(new BigDecimal(999));

        dto.setServeNo("FWD2022032800026");
        dto.setBrandId(1);
        dto.setModelsId(1);
        dto.setCreatorId(-999);

        serveAggregateRootApi.addServeForReplaceVehicle(dto);
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
        Result<ServeAdjustRecordVo> result = serveWebController.serveAdjustmentCheck(cmd, jwt);
        log.info("result----->{}", result);
    }

    @Test
    void serveAdjustment() {

        ServeAdjustCmd cmd = new ServeAdjustCmd();
        cmd.setServeNo(serveNo);
        cmd.setDepositPayType(ReplaceVehicleDepositPayTypeEnum.ACCOUNT_DEPOSIT_UNLOCK_PAY.getCode());

        serveWebController.serveAdjustment(cmd, jwt);
    }
}