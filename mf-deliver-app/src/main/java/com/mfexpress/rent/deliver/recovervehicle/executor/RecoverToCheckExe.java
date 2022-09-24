package com.mfexpress.rent.deliver.recovervehicle.executor;

import javax.annotation.Resource;

import cn.hutool.core.collection.CollectionUtil;
import com.hx.backmarket.maintain.api.domain.MaintenanceAggregateRootApi;
import com.hx.backmarket.maintain.constant.MaintenanceTypeEnum;
import com.hx.backmarket.maintain.data.cmd.maintenance.MaintenanceIdCmd;
import com.hx.backmarket.maintain.data.dto.MaintenanceDTO;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.order.api.app.ContractAggregateRootApi;
import com.mfexpress.order.constant.ContractStatusEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVechicleCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverCheckJudgeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeReplaceVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.serve.dto.ServeAdjustDTO;
import com.mfexpress.rent.deliver.dto.data.serve.qry.ServeAdjustQry;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.UsageStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class RecoverToCheckExe {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private ContractAggregateRootApi contractAggregateRootApi;
    @Resource
    private MaintenanceAggregateRootApi maintenanceAggregateRootApi;
    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    /**
     * 1. 是否为维修中
     * 2. 维修性质 故障维修不允许收车
     * 3. 是否存在替换车
     * 4. 替换车服务单是否发车
     *      1. 未发车 替换车申请是否取消
     *      2. 已发车 是否存在服务单调整单
     */
    public String execute(RecoverVechicleCmd cmd) {

        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(cmd.getServeNo());
        ResultValidUtils.checkResultException(serveDTOResult);
        ServeDTO serveDTO = serveDTOResult.getData();
        if (Objects.isNull(serveDTO)) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单不存在");
        }
        Result<VehicleInfoDto> vehicleInfoDtoResult = vehicleAggregateRootApi.getVehicleInfoVOById(cmd.getCarServiceId());
        ResultValidUtils.checkResultException(vehicleInfoDtoResult);
        VehicleInfoDto vehicleInfoDto = vehicleInfoDtoResult.getData();
        if (Objects.isNull(vehicleInfoDto)) {
            throw new CommonException(ResultErrorEnum.NOT_FOUND.getCode(), "车辆不存在");
        }
        if (vehicleInfoDto.getStatus().intValue() == UsageStatusEnum.MAINTAINING.getCode()) {
            // todo 维修申请
            // todo 车辆id查维修工单
            Result<MaintenanceDTO> maintenanceDTOResult = maintenanceAggregateRootApi.getOne(MaintenanceIdCmd.builder().maintenanceId().build());
            ResultValidUtils.checkResultException(maintenanceDTOResult);
            MaintenanceDTO maintenanceDTO = maintenanceDTOResult.getData();
            if (Objects.isNull(maintenanceDTO)) {
                throw new CommonException(ResultErrorEnum.NOT_FOUND.getCode(), "未查询到维修工单信息");
            }
            if (maintenanceDTO.getMaintenanceType() == MaintenanceTypeEnum.ACCIDENT_REPAIR.getCode()) {
                throw new CommonException(ResultErrorEnum.NOT_FOUND.getCode(), "事故维修未完成，不允许收车");
            }
            if (maintenanceDTO.getMaintenanceType() == MaintenanceTypeEnum.ACCIDENT_REPAIR.getCode()) {
                List<ServeReplaceVehicleDTO> serveReplaceVehicleDTOS = serveAggregateRootApi.getServeReplaceVehicleList();
                // 是否存在替换车
                if (CollectionUtil.isNotEmpty(serveReplaceVehicleDTOS)) {
                    ServeReplaceVehicleDTO serveReplaceVehicleDTO = serveReplaceVehicleDTOS.get(0);
                    Result<ServeDTO> sourceServeDTOResult = serveAggregateRootApi.getServeDtoByServeNo(serveReplaceVehicleDTO.getSourceServeNo());
                    ResultValidUtils.checkResultException(sourceServeDTOResult);
                    ServeDTO sourceServeDTO = sourceServeDTOResult.getData();
                    if (Objects.isNull(sourceServeDTO)) {
                        throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "替换车原车服务单不存在");
                    }
                    ServeDTO replaceServeDTO = sourceServeDTO;
                    // 替换服务单状态为0、1、2、5时，判断是否有调整工单，如果没有则不允许原车验车
                    if (Optional.ofNullable(replaceServeDTO).filter(o -> ServeEnum.NOT_PRESELECTED.getCode().equals(o.getStatus())
                            || ServeEnum.PRESELECTED.getCode().equals(o.getStatus()) || ServeEnum.DELIVER.getCode().equals(o.getStatus())
                            || ServeEnum.REPAIR.getCode().equals(o.getStatus())).isPresent()) {

                        // 查询是否存在调整工单
                        ServeAdjustQry serveAdjustQry = ServeAdjustQry.builder().serveNo(sourceServeDTO.getServeNo()).build();

                        ServeAdjustDTO serveAdjustDTO = ResultDataUtils.getInstance(serveAggregateRootApi.getServeAdjust(serveAdjustQry)).getDataOrNull();

                        if (!Optional.ofNullable(serveAdjustDTO).isPresent()) {
                            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "当前车辆存在未发车的替换单或存在替换车，无法进行收车。");
                        }
                    }
                }
            }
        }

        Result<Integer> countResult = contractAggregateRootApi.getRenewalContractCountByStatusAndServeNo(ContractStatusEnum.CREATED.getCode(), cmd.getServeNo());
        Integer count = ResultDataUtils.getInstance(countResult).getDataOrException();
        if (null == count) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "判断服务单是否已被合同续约失败");
        }
        if (0 != count) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "该服务单已被合同预续约，不支持收车操作");
        }

        return null;
    }

}

