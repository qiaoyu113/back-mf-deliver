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
        Result<MaintenanceDTO> maintenanceDTOResult = maintenanceAggregateRootApi.getMaintenanceByServeNo(sourceServNo);
        MaintenanceDTO maintenanceDTO = ResultDataUtils.getInstance(maintenanceDTOResult).getDataOrException();
        // 查找替换单
        Result<List<ReplaceVehicleDTO>> replaceVehicleDTOListResult = maintenanceAggregateRootApi.getReplaceVehicleDTOByMaintenanceId(maintenanceDTO.getMaintenanceId());
        List<ReplaceVehicleDTO> replaceVehicleDTOList = ResultDataUtils.getInstance(replaceVehicleDTOListResult).getDataOrException();
        if (CollectionUtils.isNotEmpty(replaceVehicleDTOList)) {
            ReplaceVehicleDTO replaceVehicleDTO = replaceVehicleDTOList.stream().sorted(Comparator.comparing(ReplaceVehicleDTO::getCreateTime).reversed()).collect(Collectors.toList()).get(0);
            return replaceVehicleDTO;
        }

        return null;
    }

    public static MaintenanceDTO getMaintenanceDTOByReplaceServeNo(MaintenanceAggregateRootApi maintenanceAggregateRootApi, String replaceServeNo) {

        // 查找替换单
        Result<ReplaceVehicleDTO> replaceVehicleDTOResult = maintenanceAggregateRootApi.getReplaceVehicleByServeNo(replaceServeNo);
        ReplaceVehicleDTO replaceVehicleDTO = ResultDataUtils.getInstance(replaceVehicleDTOResult).getDataOrException();
        if (replaceVehicleDTO != null) {
            // 查找维修单
            Result<MaintenanceDTO> maintenanceDTOResult = maintenanceAggregateRootApi.getMaintenanceDTOByMaintenanceId(replaceVehicleDTO.getMaintenanceId());

            return ResultDataUtils.getInstance(maintenanceDTOResult).getDataOrException();
        }

        return null;
    }
}
