package com.mfexpress.rent.deliver.utils;

import com.hx.backmarket.maintain.data.cmd.maintenance.MaintenanceIdCmd;
import com.hx.backmarket.maintain.data.cmd.maintenance.MaintenanceReplaceVehicleQryCmd;
import com.hx.backmarket.maintain.data.dto.MaintenanceDTO;
import com.hx.backmarket.maintain.data.dto.MaintenanceReplaceVehicleDTO;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.MaintenanceReplaceVehicleStatusEnum;
import com.mfexpress.rent.deliver.domainapi.proxy.backmarket.BackmarketMaintenanceAggregateRootApi;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MainServeUtil {

    /*public static ReplaceVehicleDTO getReplaceVehicleDTOBySourceServNo(MaintenanceAggregateRootApi maintenanceAggregateRootApi, String sourceServNo) {

        // 查找替换单
        Result<ReplaceVehicleDTO> replaceVehicleDTOResult = maintenanceAggregateRootApi.getReplaceVehicleDTObyMaintenanceServeNo(sourceServNo);

        return ResultDataUtils.getInstance(replaceVehicleDTOResult).getDataOrNull();
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
    }*/

    public static MaintenanceDTO getMaintenanceByMaintenanceId(BackmarketMaintenanceAggregateRootApi backmarketMaintenanceAggregateRootApi, Long maintenanceId) {
        if (null == backmarketMaintenanceAggregateRootApi || null == maintenanceId) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "参数错误");
        }
        MaintenanceIdCmd maintenanceIdCmd = new MaintenanceIdCmd();
        maintenanceIdCmd.setMaintenanceId(maintenanceId);
        Result<MaintenanceDTO> maintenanceDTOResult = backmarketMaintenanceAggregateRootApi.getOne(maintenanceIdCmd);
        MaintenanceDTO maintenanceDTO = ResultDataUtils.getInstance(maintenanceDTOResult).getDataOrNull();
        if (maintenanceDTO == null) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "未查询到维修单");
        }
        return maintenanceDTO;
    }

    public static String getReplaceServeNoBySourceServeNo(BackmarketMaintenanceAggregateRootApi backmarketMaintenanceAggregateRootApi, String sourceServeNo) {
        if (null == backmarketMaintenanceAggregateRootApi || StringUtils.isEmpty(sourceServeNo)) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "参数错误");
        }
        MaintenanceReplaceVehicleQryCmd maintenanceReplaceVehicleQry = new MaintenanceReplaceVehicleQryCmd();
        maintenanceReplaceVehicleQry.setSourceServeNoList(Collections.singletonList(sourceServeNo));
        maintenanceReplaceVehicleQry.setStatus(MaintenanceReplaceVehicleStatusEnum.TACK_EFFECT.getCode());
        Result<List<MaintenanceReplaceVehicleDTO>> maintenanceReplaceVehicleDTOSResult = backmarketMaintenanceAggregateRootApi.getMaintenanceReplaceVehicleList(maintenanceReplaceVehicleQry);
        List<MaintenanceReplaceVehicleDTO> maintenanceReplaceVehicleDTOS = ResultDataUtils.getInstance(maintenanceReplaceVehicleDTOSResult).getDataOrException();
        if (null == maintenanceReplaceVehicleDTOS || maintenanceReplaceVehicleDTOS.isEmpty()) {
            return null;
        }
        MaintenanceReplaceVehicleDTO replaceVehicleDTO = maintenanceReplaceVehicleDTOS.get(0);
        return replaceVehicleDTO.getTargetServeNo();
    }

    public static MaintenanceReplaceVehicleDTO getReplaceInfoByTargetServeNo(BackmarketMaintenanceAggregateRootApi backmarketMaintenanceAggregateRootApi, String targetServeNo) {
        if (null == backmarketMaintenanceAggregateRootApi || StringUtils.isEmpty(targetServeNo)) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "参数错误");
        }
        MaintenanceReplaceVehicleQryCmd maintenanceReplaceVehicleQry = new MaintenanceReplaceVehicleQryCmd();
        maintenanceReplaceVehicleQry.setTargetServeNoList(Collections.singletonList(targetServeNo));
        maintenanceReplaceVehicleQry.setStatus(MaintenanceReplaceVehicleStatusEnum.TACK_EFFECT.getCode());
        Result<List<MaintenanceReplaceVehicleDTO>> maintenanceReplaceVehicleDTOSResult = backmarketMaintenanceAggregateRootApi.getMaintenanceReplaceVehicleList(maintenanceReplaceVehicleQry);
        List<MaintenanceReplaceVehicleDTO> maintenanceReplaceVehicleDTOS = ResultDataUtils.getInstance(maintenanceReplaceVehicleDTOSResult).getDataOrException();
        if (null == maintenanceReplaceVehicleDTOS || maintenanceReplaceVehicleDTOS.isEmpty()) {
            return null;
        }
        return maintenanceReplaceVehicleDTOS.get(0);
    }

    public static Map<String, String> getServeNoWithReplaceServeNoMap(BackmarketMaintenanceAggregateRootApi backmarketMaintenanceAggregateRootApi, List<String> sourceServeNos) {
        if (null == backmarketMaintenanceAggregateRootApi || null == sourceServeNos || StringUtils.isEmpty(sourceServeNos)) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "参数错误");
        }
        MaintenanceReplaceVehicleQryCmd maintenanceReplaceVehicleQry = new MaintenanceReplaceVehicleQryCmd();
        maintenanceReplaceVehicleQry.setSourceServeNoList(sourceServeNos);
        maintenanceReplaceVehicleQry.setStatus(MaintenanceReplaceVehicleStatusEnum.TACK_EFFECT.getCode());
        Result<List<MaintenanceReplaceVehicleDTO>> maintenanceReplaceVehicleDTOSResult = backmarketMaintenanceAggregateRootApi.getMaintenanceReplaceVehicleList(maintenanceReplaceVehicleQry);
        List<MaintenanceReplaceVehicleDTO> maintenanceReplaceVehicleDTOS = ResultDataUtils.getInstance(maintenanceReplaceVehicleDTOSResult).getDataOrException();
        if (null == maintenanceReplaceVehicleDTOS || maintenanceReplaceVehicleDTOS.isEmpty()) {
            return null;
        }
        return maintenanceReplaceVehicleDTOS.stream().collect(Collectors.toMap(MaintenanceReplaceVehicleDTO::getSourceServeNo, MaintenanceReplaceVehicleDTO::getTargetServeNo, (v1, v2) -> v1));
    }

}
