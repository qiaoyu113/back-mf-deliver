package com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd;

import com.mfexpress.rent.deliver.MfDeliveryApplication;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverVehicleProcessCmd;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.DateFormat;
import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MfDeliveryApplication.class)
class RecoverVehicleProcessCmdExeTest {

    @Resource
    RecoverVehicleProcessCmdExe recoverVehicleProcessCmdExe;

    @Test
    void execute() {

        RecoverVehicleProcessCmd cmd = new RecoverVehicleProcessCmd();

        cmd.setServeNo("FWD2022061600012");
        cmd.setContactId(125007763561300011l);
        cmd.setCarId(6690);
        cmd.setDeliverNo("JFD2022061600013");
        cmd.setContractForeignNo("2975286643675693156");
        cmd.setRecoverWareHouseId(15);
        cmd.setRecoverVehicleTime(FormatUtil.ymdFormatStringToDate("2022-06-17"));
        cmd.setExpectRecoverDate("2022-06-17");
        cmd.setCustomerId(1243);
        cmd.setServeStatus(5);

        recoverVehicleProcessCmdExe.execute(cmd);
    }
}