package com.mfexpress.rent.deliver.recovervehicle.executor;

import javax.annotation.Resource;

import com.hx.backmarket.maintain.constant.MaintenanceNatureEnum;
import com.hx.backmarket.maintain.constant.MaintenanceStatusEnum;
import com.hx.backmarket.maintain.constant.MaintenanceTypeEnum;
import com.hx.backmarket.maintain.constant.maintainapply.MaintainApplyStatusEnum;
import com.hx.backmarket.maintain.data.cmd.maintainapply.MaintainApplyListQry;
import com.hx.backmarket.maintain.data.cmd.maintenance.MaintenanceIdCmd;
import com.hx.backmarket.maintain.data.cmd.maintenance.MaintenanceReplaceVehicleQryCmd;
import com.hx.backmarket.maintain.data.dto.MaintenanceDTO;
import com.hx.backmarket.maintain.data.dto.MaintenanceReplaceVehicleDTO;
import com.hx.backmarket.maintain.data.dto.maintainapply.MaintainApplyDTO;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.PagePagination;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.order.api.app.ContractAggregateRootApi;
import com.mfexpress.order.constant.ContractStatusEnum;
import com.mfexpress.rent.deliver.constant.MaintenanceReplaceVehicleStatusEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.proxy.backmarket.BackmarketMaintainApplyAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.proxy.backmarket.BackmarketMaintenanceAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVechicleCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeRepairDTO;
import com.mfexpress.rent.deliver.dto.data.serve.dto.ServeAdjustDTO;
import com.mfexpress.rent.deliver.dto.data.serve.qry.ServeAdjustQry;
import com.mfexpress.rent.deliver.utils.MainServeUtil;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
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
    private BackmarketMaintainApplyAggregateRootApi backmarketMaintainApplyAggregateRootApi;
    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private BackmarketMaintenanceAggregateRootApi backmarketMaintenanceAggregateRootApi;

    /**
     * 1. 是否为维修中
     * 2. 维修性质 事故维修不允许收车
     * 3. 是否存在替换车
     * 4. 替换车服务单是否发车
     * 1. 未发车 替换车申请是否取消
     * 2. 已发车 是否存在服务单调整单
     * 5. 判断服务单是否已被合同续约
     */
    public Boolean execute(RecoverVechicleCmd cmd) {

        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(cmd.getServeNo());
        ResultValidUtils.checkResultException(serveDTOResult);
        ServeDTO serveDTO = serveDTOResult.getData();
        if (Objects.isNull(serveDTO)) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单不存在");
        }
        Result<DeliverDTO> deliverResult = deliverAggregateRootApi.getDeliverByServeNo(cmd.getServeNo());
        ResultValidUtils.checkResultException(deliverResult);
        DeliverDTO deliverDTO = deliverResult.getData();
        if (Objects.isNull(deliverDTO)) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "交付单不存在");
        }
        Result<VehicleInfoDto> vehicleInfoDtoResult = vehicleAggregateRootApi.getVehicleInfoVOById(deliverDTO.getCarId());
        ResultValidUtils.checkResultException(vehicleInfoDtoResult);
        VehicleInfoDto vehicleInfoDto = vehicleInfoDtoResult.getData();
        if (Objects.isNull(vehicleInfoDto)) {
            throw new CommonException(ResultErrorEnum.NOT_FOUND.getCode(), "车辆不存在");
        }

        // 待定，服务单在什么时候变为维修中状态，是创建维修申请还是维修申请审核通过？维修申请审核通过
        // 如果服务单不是维修状态，判断其有没有进行中的维修申请，如果有，提示其对维修申请做处理
        if (ServeEnum.REPAIR.getCode().equals(serveDTO.getStatus())) {
            Result<List<ServeRepairDTO>> serveRepairDTOSResult = serveAggregateRootApi.getServeRepairDTOSByServeNo(serveDTO.getServeNo());
            List<ServeRepairDTO> serveRepairDTOS = serveRepairDTOSResult.getData();
            if (null == serveRepairDTOS || serveRepairDTOS.isEmpty()) {
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "维修记录查询失败");
            }
            ServeRepairDTO serveRepairDTO = serveRepairDTOS.get(0);
            MaintenanceIdCmd maintenanceIdCmd = new MaintenanceIdCmd();
            maintenanceIdCmd.setMaintenanceId(serveRepairDTO.getMaintenanceId());
            Result<MaintenanceDTO> maintenanceDTOResult = backmarketMaintenanceAggregateRootApi.getOne(maintenanceIdCmd);
            MaintenanceDTO maintenanceDTO = maintenanceDTOResult.getData();
            if (null == maintenanceDTO) {
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "维修单查询失败");
            }
            if (MaintenanceTypeEnum.FAULT_REPAIR.getCode() == maintenanceDTO.getMaintenanceType() && (MaintenanceStatusEnum.WAIT_REPAIR.getCode() == maintenanceDTO.getMaintenanceStatus() ||
                    MaintenanceStatusEnum.REPAIRING.getCode() == maintenanceDTO.getMaintenanceStatus() || MaintenanceStatusEnum.WAIT_FETCH.getCode() == maintenanceDTO.getMaintenanceStatus())) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "当前车辆处于事故维修中，无法进行收车。");
            } else {
                // 查找替换车服务单
                String replaceServeNo = MainServeUtil.getReplaceServeNoBySourceServeNo(backmarketMaintenanceAggregateRootApi, cmd.getServeNo());
                if (!StringUtils.isEmpty(replaceServeNo)) {
                    Result<ServeDTO> replaceServeDTOResult = serveAggregateRootApi.getServeDtoByServeNo(replaceServeNo);
                    ServeDTO replaceServeDTO = ResultDataUtils.getInstance(replaceServeDTOResult).getDataOrException();
                    if (null == replaceServeDTO) {
                        throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "替换车服务单查询失败");
                    }
                    if (ServeEnum.NOT_PRESELECTED.getCode().equals(replaceServeDTO.getStatus()) || ServeEnum.PRESELECTED.getCode().equals(replaceServeDTO.getStatus()) || ServeEnum.DELIVER.getCode().equals(replaceServeDTO.getStatus())
                            || ServeEnum.REPAIR.getCode().equals(replaceServeDTO.getStatus())) {
                        // 查询是否存在调整工单
                        ServeAdjustQry serveAdjustQry = ServeAdjustQry.builder().serveNo(replaceServeDTO.getServeNo()).build();
                        ServeAdjustDTO serveAdjustDTO = ResultDataUtils.getInstance(serveAggregateRootApi.getServeAdjust(serveAdjustQry)).getDataOrNull();
                        if (!Optional.ofNullable(serveAdjustDTO).isPresent()) {
                            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "当前车辆存在未发车的替换单或存在替换车，无法进行收车。");
                        }
                    }
                }
            }
        } else {
            MaintainApplyListQry maintainApplyListQry = new MaintainApplyListQry();
            maintainApplyListQry.setVehicleId(deliverDTO.getCarId());
            Result<PagePagination<MaintainApplyDTO>> maintainApplyResult = backmarketMaintainApplyAggregateRootApi.list(maintainApplyListQry);
            PagePagination<MaintainApplyDTO> maintainApplyDTOPage = ResultDataUtils.getInstance(maintainApplyResult).getDataOrException();
            if (null != maintainApplyDTOPage && null != maintainApplyDTOPage.getList() && !maintainApplyDTOPage.getList().isEmpty()) {
                List<MaintainApplyDTO> maintainApplyDTOS = maintainApplyDTOPage.getList();
                MaintainApplyDTO maintainApplyDTO = maintainApplyDTOS.get(0);
                if (MaintenanceNatureEnum.RENT_REPAIR.getCode() == maintainApplyDTO.getMaintenanceType()) {
                    if (!MaintainApplyStatusEnum.CANCELED.getCode().equals(maintainApplyDTO.getStatus())) {
                        throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "当前车辆存在未完成的维修申请，请完成后重试。");
                    }
                }
            }
        }

        Result<Integer> contractCountResult = contractAggregateRootApi.getRenewalContractCountByStatusAndServeNo(ContractStatusEnum.CREATED.getCode(), cmd.getServeNo());
        Integer count = ResultDataUtils.getInstance(contractCountResult).getDataOrException();
        if (null == count) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "判断服务单是否已被合同续约失败");
        }
        if (0 != count) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "该服务单已被合同预续约，不支持收车操作");
        }

        return Boolean.TRUE;
    }

}

