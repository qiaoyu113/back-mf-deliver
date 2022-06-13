package com.mfexpress.rent.deliver.utils;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.dto.data.MaintenanceDTO;
import com.mfexpress.rent.maintain.dto.data.ReplaceVehicleDTO;

public class MainServeUtil {

    public static ReplaceVehicleDTO getReplaceVehicleDTOBySourceServNo(MaintenanceAggregateRootApi maintenanceAggregateRootApi, String sourceServNo) {

        // 查找维修单
        Result<ReplaceVehicleDTO> replaceVehicleDTOResult = maintenanceAggregateRootApi.getReplaceVehicleDTObyMaintenanceServeNo(sourceServNo);

        return ResultDataUtils.getInstance(replaceVehicleDTOResult).getDataOrException();
    }

    public static MaintenanceDTO getMaintenanceDTOByReplaceServeNo(MaintenanceAggregateRootApi maintenanceAggregateRootApi, String replaceServeNo) {

        Result<MaintenanceDTO> maintenanceDTOResult = maintenanceAggregateRootApi.getMaintenanceDTOByReplaceVehicleServeNo(replaceServeNo);

        MaintenanceDTO maintenanceDTO = ResultDataUtils.getInstance(maintenanceDTOResult).getDataOrNull();

        if (maintenanceDTO == null) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "未查询到原车维修单");
        }

        return maintenanceDTO;
    }

    public static MaintenanceDTO getMaintenanceByServeNo(MaintenanceAggregateRootApi maintenanceAggregateRootApi, String serveNo) {

        Result<MaintenanceDTO> maintenanceDTOResult = maintenanceAggregateRootApi.getMaintenanceByServeNoAll(serveNo);

        MaintenanceDTO maintenanceDTO = ResultDataUtils.getInstance(maintenanceDTOResult).getDataOrNull();

        if (maintenanceDTO == null) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "未查询到维修单");
        }

        return maintenanceDTO;
    }
}
