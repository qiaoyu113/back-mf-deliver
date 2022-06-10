package com.mfexpress.rent.deliver.mobile;

import java.util.List;

import javax.annotation.Resource;

import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.MfDeliveryApplication;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverApplyQryCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverApplyVO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverCancelByDeliverCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVechicleCmd;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MfDeliveryApplication.class)
class RecoverVehicleControllerTest {

    @Resource
    RecoverVehicleController controller;

    String jwt = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySW5mbyI6ImV5SmhZMk52ZFc1MFRtOXVSWGh3YVhKbFpDSTZkSEoxWlN3aVlXTmpiM1Z1ZEU1dmJreHZZMnRsWkNJNmRISjFaU3dpWVhWMGFHVnVkR2xqWVhSbFpDSTZkSEoxWlN3aVluVlVlWEJsSWpvd0xDSmphWFI1U1dRaU9qTXNJbU52Y25CVmMyVnlTV1FpT2lJaUxDSmpjbVZoZEdWRVlYUmxJam94TmpNeE9UTXpOakF4TURBd0xDSmpjbVZoZEdWSlpDSTZNQ3dpWTNKbFpHVnVkR2xoYkhOT2IyNUZlSEJwY21Wa0lqcDBjblZsTENKa1pXeEdiR0ZuSWpvd0xDSmtkWFI1U1dRaU9qWXNJbVZ1WVdKc1pXUWlPblJ5ZFdVc0ltbGtJam95TkN3aWJXOWlhV3hsSWpvaU1UTTRNREF4TXpnd01EQWlMQ0p1YVdOclRtRnRaU0k2SXVtVWdPV1VydWU3aitlUWhsL25wNS9vdFlFbzVZeVg1THFzNzd5Sklpd2liMlptYVdObFNXUWlPakU1TENKd1lYTnpkMjl5WkNJNklpSXNJbkp2YkdWSlpDSTZNalU0TENKelpYUjBhVzVuUm14aFp5STZNU3dpYzNSaGRIVnpJam94TENKMGIydGxia1Y0Y0dseVpXUWlPakUyTmpJeU5UWTNOalkzTURnc0luUjVjR1VpT2pBc0luVndaR0YwWlVSaGRHVWlPakUyTlRJME1qUXpPVFV3TURBc0luVndaR0YwWlVsa0lqb3hMQ0oxYzJWeWJtRnRaU0k2SWpFek9EQXdNVE00TURBd0luMD0iLCJzdWIiOiIxMzgwMDEzODAwMCIsImV4cCI6MTY2MjI1Njc2Nn0.G0O0FeEKvjm9as6zQse9twxPgIViTJg1ro6ceqbaj9tQ00-HKxmCekK_cLr-js8hoQ_ypQjWTheUtear1J2ksg";

    // TODO 服务单号
    String serveNo = "";

    // TODO 维修单号
    String deliverNo = "";
    @Test
    void getRecoverVehicleListVO() {

        RecoverApplyQryCmd cmd = new RecoverApplyQryCmd();
        cmd.setCustomerId(85);

        Result<List<RecoverApplyVO>> result = controller.getRecoverVehicleListVO(cmd, jwt);

        log.info("size---->{}----result---->{}", ResultDataUtils.getInstance(result).getDataOrException().size(), result);
    }

    @Test
    void applyRecover() {
    }

    @Test
    void cancelRecover() {
    }

    @Test
    void cancelRecoverByDeliver() {

        // 取消收车 判断替换车是否调整为正常服务单
        RecoverCancelByDeliverCmd cmd = new RecoverCancelByDeliverCmd();
        cmd.setDeliverNo(deliverNo);

        controller.cancelRecoverByDeliver(cmd, jwt);
    }

    @Test
    void getRecoverListVO() {
    }

    @Test
    void getRecoverTaskListVO() {
    }

    @Test
    void testGetRecoverVehicleListVO() {
    }

    @Test
    void testApplyRecover() {
    }

    @Test
    void testCancelRecover() {
    }

    @Test
    void testCancelRecoverByDeliver() {
    }

    @Test
    void testGetRecoverListVO() {
    }

    @Test
    void testGetRecoverTaskListVO() {
    }

    @Test
    void whetherToCheck() {
        RecoverVechicleCmd cmd = new RecoverVechicleCmd();
        cmd.setServeNo("FWD2021092600192");

        Result<String> result = controller.whetherToCheck(cmd, jwt);

        log.info("result---->{}", result);
    }

    @Test
    void toBackInsure() {
    }

    @Test
    void testToBackInsure() {
    }

    @Test
    void toDeduction() {
    }

    @Test
    void toDeductionByDeliver() {
    }

    @Test
    void cacheCheckInfo() {
    }

    @Test
    void getCachedCheckInfo() {
    }

    @Test
    void getRecoverDetail() {
    }

    @Test
    void abnormalRecover() {
    }

    @Test
    void getRecoverAbnormalInfo() {
    }
}