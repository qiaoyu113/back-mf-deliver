package com.mfexpress.rent.deliver.utils;

import javax.annotation.Resource;

import com.mfexpress.rent.deliver.MfDeliveryApplication;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.dto.data.MaintenanceDTO;
import com.mfexpress.rent.maintain.dto.data.ReplaceVehicleDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MfDeliveryApplication.class)
class MainServeUtilTest {

    @Resource
    MaintenanceAggregateRootApi maintenanceAggregateRootApi;

    @Test
    void getReplaceVehicleDTOBySourceServNo() {

        ReplaceVehicleDTO replaceVehicleDTO = MainServeUtil.getReplaceVehicleDTOBySourceServNo(maintenanceAggregateRootApi, "FWD2022042800017");

        log.info("{}", replaceVehicleDTO);
    }

    @Test
    void getMaintenanceDTOByReplaceServeNo() {

        MaintenanceDTO maintenanceDTO = MainServeUtil.getMaintenanceDTOByReplaceServeNo(maintenanceAggregateRootApi, "FWD2022042800017");

        log.info("{}", maintenanceDTO);
    }

    @Test
    void getMaintenanceByServeNo() {

        MaintenanceDTO maintenanceDTO = MainServeUtil.getMaintenanceByServeNo(maintenanceAggregateRootApi, "FWD2022061700008");

        log.info("{}", maintenanceDTO);
    }
}