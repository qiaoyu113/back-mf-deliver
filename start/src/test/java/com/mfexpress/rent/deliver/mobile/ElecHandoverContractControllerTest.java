package com.mfexpress.rent.deliver.mobile;

import java.util.Date;

import javax.annotation.Resource;

import com.mfexpress.rent.deliver.MfDeliveryApplication;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CreateDeliverContractCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CreateRecoverContractFrontCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.RecoverInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MfDeliveryApplication.class)
class ElecHandoverContractControllerTest {

    @Resource
    ElecHandoverContractController controller;

    String jwt = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySW5mbyI6ImV5SmhZMk52ZFc1MFRtOXVSWGh3YVhKbFpDSTZkSEoxWlN3aVlXTmpiM1Z1ZEU1dmJreHZZMnRsWkNJNmRISjFaU3dpWVhWMGFHVnVkR2xqWVhSbFpDSTZkSEoxWlN3aVluVlVlWEJsSWpvd0xDSmphWFI1U1dRaU9qRXNJbU52Y25CVmMyVnlTV1FpT2lJaUxDSmpjbVZoZEdWRVlYUmxJam94TmpBNU1qRXlNelU1TURBd0xDSmpjbVZoZEdWSlpDSTZNQ3dpWTNKbFpHVnVkR2xoYkhOT2IyNUZlSEJwY21Wa0lqcDBjblZsTENKa1pXeEdiR0ZuSWpvd0xDSmtkWFI1U1dRaU9qazVPVGtzSW1WdVlXSnNaV1FpT25SeWRXVXNJbWxrSWpvdE9UazVMQ0p0YjJKcGJHVWlPaUl4TXpNd01EQXdNREF3TUNJc0ltNXBZMnRPWVcxbElqb2k1N083NTd1Zklpd2liMlptYVdObFNXUWlPakVzSW5CaGMzTjNiM0prSWpvaUlpd2ljbTlzWlVsa0lqb3dMQ0p6WlhSMGFXNW5SbXhoWnlJNk1Td2ljM1JoZEhWeklqb3hMQ0owYjJ0bGJrVjRjR2x5WldRaU9qRTJOakV6TURjek1UazBOVFFzSW5SNWNHVWlPakFzSW5Wd1pHRjBaVVJoZEdVaU9qRTJOVEl3T0RNNU16QXdNREFzSW5Wd1pHRjBaVWxrSWpvd0xDSjFjMlZ5Ym1GdFpTSTZJakV6TXpBd01EQXdNREF3SW4wPSIsInN1YiI6IjEzMzAwMDAwMDAwIiwiZXhwIjoxNjYxMzA3MzE5fQ.W8kKqkMGychXeCUUyI4zYuMkUo9cVF7caezL2zeZCmxWedu6YnccGSq7MP1TlUBUkGgcOAadl38w0XkKuHmajg";


    // TODO 服务单号
    String serveNo = "FWD2022032900005";

    String contactsCard = "142702198701153245";

    String contactsName = "junit";

    String contactPhone = "18634853241";

    String imgUrl = "blob:http://dev.baic-mfexpress.com:8080/207295b5-0991-4e62-abda-50cba461d865";

    @Test
    void createDeliverContract() {

        CreateDeliverContractCmd cmd = new CreateDeliverContractCmd();
        DeliverInfo deliverInfo = new DeliverInfo();
        deliverInfo.setContactsCard(contactsCard);
        deliverInfo.setContactsName(contactsName);
        deliverInfo.setContactsPhone(contactPhone);
        deliverInfo.setDeliverVehicleTime(new Date());

        cmd.setDeliverInfo(deliverInfo);
        controller.createDeliverContract(cmd, jwt);
    }

    @Test
    void createRecoverContract() {

        CreateRecoverContractFrontCmd cmd = new CreateRecoverContractFrontCmd();
        RecoverInfo recoverInfo = new RecoverInfo();
        recoverInfo.setServeNo(serveNo);
        recoverInfo.setContactsCard(contactsCard);
        recoverInfo.setContactsName(contactsName);
        recoverInfo.setContactsPhone(contactPhone);
        recoverInfo.setRecoverVehicleTime(new Date());
        recoverInfo.setDamageFee(0.00);
        recoverInfo.setWareHouseId(0);
        recoverInfo.setImgUrl(imgUrl);

        cmd.setRecoverInfo(recoverInfo);

        controller.createRecoverContract(cmd, jwt);
    }
}