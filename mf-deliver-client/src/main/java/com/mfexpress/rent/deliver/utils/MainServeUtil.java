package com.mfexpress.rent.deliver.utils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.dto.data.MaintenanceDTO;
import com.mfexpress.rent.maintain.dto.data.ReplaceVehicleDTO;
import org.apache.commons.collections.CollectionUtils;

public class MainServeUtil {

    public static ReplaceVehicleDTO getReplaceVehicleDTOBySourceServNo(MaintenanceAggregateRootApi maintenanceAggregateRootApi, String sourceServNo) {

        // 查找维修单
        Result<ReplaceVehicleDTO> replaceVehicleDTOResult = maintenanceAggregateRootApi.getReplaceVehicleDTObyMaintenanceServeNo(sourceServNo);

        return ResultDataUtils.getInstance(replaceVehicleDTOResult).getDataOrException();
    }

    public static MaintenanceDTO getMaintenanceDTOByReplaceServeNo(MaintenanceAggregateRootApi maintenanceAggregateRootApi, String replaceServeNo) {

        Result<MaintenanceDTO> maintenanceDTOResult = maintenanceAggregateRootApi.getMaintenanceDTOByReplaceVehicleServeNo(replaceServeNo);

        return ResultDataUtils.getInstance(maintenanceDTOResult).getDataOrException();
    }
}
