package com.mfexpress.rent.deliver.utils;

import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.dto.data.MaintenanceDTO;
import com.mfexpress.rent.maintain.dto.data.ReplaceVehicleDTO;

public class MainServeUtil {

    public static ReplaceVehicleDTO getReplaceVehicleDTOBySourceServNo(MaintenanceAggregateRootApi maintenanceAggregateRootApi, String sourceServNo) {

        // 查找维修单
        Result<ReplaceVehicleDTO> replaceVehicleDTOResult = maintenanceAggregateRootApi.getReplaceVehicleDTObyMaintenanceServeNo(sourceServNo);

        return ResultDataUtils.getInstance(replaceVehicleDTOResult).getDataOrNull();
    }

    public static MaintenanceDTO getMaintenanceDTOByReplaceServeNo(MaintenanceAggregateRootApi maintenanceAggregateRootApi, String replaceServeNo) {

        Result<MaintenanceDTO> maintenanceDTOResult = maintenanceAggregateRootApi.getMaintenanceDTOByReplaceVehicleServeNo(replaceServeNo);

        return ResultDataUtils.getInstance(maintenanceDTOResult).getDataOrException();
    }
}
