package com.mfexpress.rent.deliver.mobile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.mfexpress.rent.deliver.MfDeliveryApplication;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CreateDeliverContractCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CreateRecoverContractFrontCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverImgInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.RecoverInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MfDeliveryApplication.class)
class ElecHandoverContractControllerTest {

    @Resource
    ElecHandoverContractController controller;

    String jwt = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySW5mbyI6ImV5SmhZMk52ZFc1MFRtOXVSWGh3YVhKbFpDSTZkSEoxWlN3aVlXTmpiM1Z1ZEU1dmJreHZZMnRsWkNJNmRISjFaU3dpWVhWMGFHVnVkR2xqWVhSbFpDSTZkSEoxWlN3aVluVlVlWEJsSWpvd0xDSmphWFI1U1dRaU9qSXNJbU52Y25CVmMyVnlTV1FpT2lJaUxDSmpjbVZoZEdWRVlYUmxJam94TmpNeU56WTVPVEl3TURBd0xDSmpjbVZoZEdWSlpDSTZNQ3dpWTNKbFpHVnVkR2xoYkhOT2IyNUZlSEJwY21Wa0lqcDBjblZsTENKa1pXeEdiR0ZuSWpvd0xDSmtkWFI1U1dRaU9qRXdMQ0psYm1GaWJHVmtJanAwY25WbExDSnBaQ0k2TVRVc0ltMXZZbWxzWlNJNklqRXpPREV4TURFeE16UXdJaXdpYm1samEwNWhiV1VpT2lMb2thUG5ncHdpTENKdlptWnBZMlZKWkNJNk1Ua3NJbkJoYzNOM2IzSmtJam9pSWl3aWNtOXNaVWxrSWpvd0xDSnpaWFIwYVc1blJteGhaeUk2TUN3aWMzUmhkSFZ6SWpveExDSjBiMnRsYmtWNGNHbHlaV1FpT2pFMk5qSTFORE00TkRRNE9EQXNJblI1Y0dVaU9qQXNJblZ3WkdGMFpVUmhkR1VpT2pFMk5USTBNelEwTkRnd01EQXNJblZ3WkdGMFpVbGtJam93TENKMWMyVnlibUZ0WlNJNklqRXpPREV4TURFeE16UXdJbjA9Iiwic3ViIjoiMTM4MTEwMTEzNDAiLCJleHAiOjE2NjI1NDM4NDR9.K0GTq1JwsSslnJOyC3jk5Sp6ky_CG6VfBDO44OVNBI6OjgtE1U4U39_pnA6riWcsrHY54WQZEFTLcwlFL7jEjA";


    // TODO 服务单号
    String serveNo = "FWD2022031700008";

    String contactsCard = "142702198701153245";

    String contactsName = "junit";

    String contactPhone = "18634853241";

    String imgUrl = "http://dev-mf-common-bucket.oss-cn-hangzhou.aliyuncs.com/mfh5/202259/1654777468000a5KhzbrPaWytyK2W3CSHdK4S34DjnpQR.jpg";

    @Test
    void createDeliverContract() {

        CreateDeliverContractCmd cmd = new CreateDeliverContractCmd();
        DeliverInfo deliverInfo = new DeliverInfo();

        deliverInfo.setContactsCard(contactsCard);
        deliverInfo.setContactsName(contactsName);
        deliverInfo.setContactsPhone(contactPhone);
        deliverInfo.setDeliverVehicleTime(new Date());

        cmd.setDeliverInfo(deliverInfo);

        List<DeliverImgInfo> deliverImgInfos = new ArrayList<>();
        DeliverImgInfo deliverImgInfo = new DeliverImgInfo();
        deliverImgInfo.setServeNo(serveNo);
        deliverImgInfo.setCarNum("京A000025");
        deliverImgInfo.setCarId(5358);
        deliverImgInfo.setImgUrl(imgUrl);
        deliverImgInfo.setDeliverNo("JFD2022031700005");

        deliverImgInfos.add(deliverImgInfo);

        cmd.setDeliverImgInfos(deliverImgInfos);
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