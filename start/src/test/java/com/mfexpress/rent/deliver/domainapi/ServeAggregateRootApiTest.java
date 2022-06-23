package com.mfexpress.rent.deliver.domainapi;

import java.util.Optional;

import javax.annotation.Resource;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.MfDeliveryApplication;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverCheckJudgeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeCancelCmd;
import com.mfexpress.rent.deliver.dto.data.serve.dto.ServeAdjustDTO;
import com.mfexpress.rent.deliver.dto.data.serve.qry.ServeAdjustQry;
import com.mfexpress.rent.deliver.utils.MainServeUtil;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.constant.MaintenanceStatusEnum;
import com.mfexpress.rent.maintain.constant.MaintenanceTypeEnum;
import com.mfexpress.rent.maintain.dto.data.MaintenanceDTO;
import com.mfexpress.rent.maintain.dto.data.ReplaceVehicleDTO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@RunWith(Runner.class)
@SpringBootTest(classes = MfDeliveryApplication.class)
class ServeAggregateRootApiTest {

    @Resource
    ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    MaintenanceAggregateRootApi maintenanceAggregateRootApi;

    String jwt = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySW5mbyI6ImV5SmhZMk52ZFc1MFRtOXVSWGh3YVhKbFpDSTZkSEoxWlN3aVlXTmpiM1Z1ZEU1dmJreHZZMnRsWkNJNmRISjFaU3dpWVhWMGFHVnVkR2xqWVhSbFpDSTZkSEoxWlN3aVluVlVlWEJsSWpvd0xDSmphWFI1U1dRaU9qTXNJbU52Y25CVmMyVnlTV1FpT2lJaUxDSmpjbVZoZEdWRVlYUmxJam94TmpNeE9UTXpOakF4TURBd0xDSmpjbVZoZEdWSlpDSTZNQ3dpWTNKbFpHVnVkR2xoYkhOT2IyNUZlSEJwY21Wa0lqcDBjblZsTENKa1pXeEdiR0ZuSWpvd0xDSmtkWFI1U1dRaU9qWXNJbVZ1WVdKc1pXUWlPblJ5ZFdVc0ltbGtJam95TkN3aWJXOWlhV3hsSWpvaU1UTTRNREF4TXpnd01EQWlMQ0p1YVdOclRtRnRaU0k2SXVtVWdPV1VydWU3aitlUWhsL25wNS9vdFlFbzVZeVg1THFzNzd5Sklpd2liMlptYVdObFNXUWlPakU1TENKd1lYTnpkMjl5WkNJNklpSXNJbkp2YkdWSlpDSTZNalU0TENKelpYUjBhVzVuUm14aFp5STZNU3dpYzNSaGRIVnpJam94TENKMGIydGxia1Y0Y0dseVpXUWlPakUyTmpJeU5UWTNOalkzTURnc0luUjVjR1VpT2pBc0luVndaR0YwWlVSaGRHVWlPakUyTlRJME1qUXpPVFV3TURBc0luVndaR0YwWlVsa0lqb3hMQ0oxYzJWeWJtRnRaU0k2SWpFek9EQXdNVE00TURBd0luMD0iLCJzdWIiOiIxMzgwMDEzODAwMCIsImV4cCI6MTY2MjI1Njc2Nn0.G0O0FeEKvjm9as6zQse9twxPgIViTJg1ro6ceqbaj9tQ00-HKxmCekK_cLr-js8hoQ_ypQjWTheUtear1J2ksg";

    @Test
    void cancelServe() {
        ServeCancelCmd cmd = new ServeCancelCmd();
        cmd.setServeNo("FWD2022061400038");
        cmd.setOperatorId(-999);

        Result<Integer> result = serveAggregateRootApi.cancelServe(cmd);

        ResultValidUtils.checkResultException(result);

    }

    @Test
    void recoverCheckJudge() {

        RecoverCheckJudgeCmd cmd = new RecoverCheckJudgeCmd();
        cmd.setServeNo("FWD2022062200029");

        serveAggregateRootApi.recoverCheckJudge(cmd);
    }
}