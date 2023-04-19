package com.mfexpress.rent.deliver.recovervehicle.executor;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.billing.rentcharge.dto.data.deliver.RenewalCmd;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.component.starter.tools.mq.MqTools;
import com.mfexpress.component.utils.util.DateUtils;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.config.DeliverProjectProperties;
import com.mfexpress.rent.deliver.constant.DeliverContractStatusEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.DailyAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.daily.CreateDailyCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleCmd;
import com.mfexpress.rent.deliver.dto.data.serve.RenewalChargeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustStartBillingCmd;
import com.mfexpress.rent.deliver.dto.data.serve.dto.ServeAdjustDTO;
import com.mfexpress.rent.deliver.dto.data.serve.qry.ServeAdjustQry;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import com.mfexpress.rent.deliver.utils.MainServeUtil;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.constant.MaintenanceStatusEnum;
import com.mfexpress.rent.maintain.constant.MaintenanceTypeEnum;
import com.mfexpress.rent.maintain.dto.data.MaintenanceDTO;
import com.mfexpress.rent.maintain.dto.data.ReplaceVehicleDTO;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.WarehouseAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.UsageStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidStockStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.rent.vehicle.data.dto.warehouse.WarehouseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j
public class RecoverVehicleCmdExe {

    @Resource
    private MaintenanceAggregateRootApi maintenanceAggregateRootApi;

    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private WarehouseAggregateRootApi warehouseAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private DailyAggregateRootApi dailyAggregateRootApi;

    @Resource(name = "serveSyncServiceImpl")
    private EsSyncHandlerI serveSyncServiceI;

    @Resource(name = "deliverSyncServiceImpl")
    private EsSyncHandlerI deliverSyncServiceI;

    @Resource
    private MqTools mqTools;

    @Value("${rocketmq.listenEventTopic}")
    private String event;


    public Integer execute(RecoverVehicleCmd cmd, TokenInfo tokenInfo) {
        // 收车日期校验
        checkDate(cmd);
        // 交付单状态检查，收车电子合同签署状态应为0未签署
        recoverCheck(cmd);

        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(cmd.getServeNo());
        ServeDTO serveDTO = ResultDataUtils.getInstance(serveDTOResult).getDataOrException();
        if (null == serveDTO) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "服务单查询失败");
        }

        // 服务单、交付单、收车单修改
        cmd.setOperatorId(tokenInfo.getId());
        cmd.setOperatorName(tokenInfo.getNickName());
        serveAggregateRootApi.recoverVehicle(cmd);

        // 更新车辆状态
        updateVehicle(cmd);

        // 发送收车计费信息
        ceaseBilling(cmd, serveDTO);

        // 创建日报
        createDaily(cmd);

        // 手动同步es
        Map<String, String> map = new HashMap<>();
        map.put("serve_no", cmd.getServeNo());
        serveSyncServiceI.execOne(map);

        map.put("deliver_no", cmd.getDeliverNo());
        deliverSyncServiceI.execOne(map);

        return 0;
    }

    private void createDaily(RecoverVehicleCmd cmd) {
        CreateDailyCmd createDailyCmd = new CreateDailyCmd();
        createDailyCmd.setDeliverFlag(0);
        createDailyCmd.setDeliverRecoverDate(cmd.getRecoverVehicleTime());
        createDailyCmd.setServeNoList(Collections.singletonList(cmd.getServeNo()));
        dailyAggregateRootApi.createDaily(createDailyCmd);
    }

    private void ceaseBilling(RecoverVehicleCmd cmd, ServeDTO serveDTO) {
        Date recoverVehicleTime = cmd.getRecoverVehicleTime();
        DateTime expectRecoverDate = DateUtil.parseDate(serveDTO.getExpectRecoverDate());
        // 判断实际收车日期和预计收车日期的前后关系，如果实际收车日期在预计收车日期之前或当天，发送收车计费消息，反之，发送自动续约消息
        if (expectRecoverDate.isAfterOrEquals(recoverVehicleTime)) {
            //收车计费
            com.mfexpress.billing.rentcharge.dto.data.deliver.RecoverVehicleCmd recoverVehicleCmd = new com.mfexpress.billing.rentcharge.dto.data.deliver.RecoverVehicleCmd();
            recoverVehicleCmd.setServeNo(cmd.getServeNo());
            recoverVehicleCmd.setVehicleId(cmd.getCarId());
            recoverVehicleCmd.setDeliverNo(cmd.getDeliverNo());
            recoverVehicleCmd.setCustomerId(serveDTO.getCustomerId());
            recoverVehicleCmd.setCreateId(cmd.getOperatorId());
            recoverVehicleCmd.setRecoverDate(DateUtil.formatDate(cmd.getRecoverVehicleTime()));
            recoverVehicleCmd.setBusinessType(serveDTO.getBusinessType());
            log.info("正常收车时，交付域向计费域发送的收车单信息：{}", recoverVehicleCmd);
            mqTools.send(event, "recover_vehicle", null, JSON.toJSONString(recoverVehicleCmd));
            // 服务单维修中
            if (ServeEnum.REPAIR.getCode().equals(serveDTO.getStatus())) {
                // 查找维修单
                MaintenanceDTO maintenanceDTO = MainServeUtil.getMaintenanceByServeNo(maintenanceAggregateRootApi, cmd.getServeNo());
                // 维修性质为故障维修
                if (MaintenanceTypeEnum.FAULT.getCode().intValue() == maintenanceDTO.getType()) {
                    // 原车维修单变为库存中维修
                    ResultValidUtils.checkResultException(maintenanceAggregateRootApi.updateMaintenanceDetailByServeNo(cmd.getServeNo()));
                    if (MaintenanceStatusEnum.MAINTAINED.getCode().equals(maintenanceDTO.getStatus())) {
                        // 维修单将在维修域由租赁中维修变更为库存中维修，如果其状态在已维修，需要变更为已完成，同时需要通知车辆修改其状态为正常
                        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
                        vehicleSaveCmd.setId(Collections.singletonList(maintenanceDTO.getVehicleId()));
                        vehicleSaveCmd.setStatus(UsageStatusEnum.NORMAL.getCode());
                        vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
                    }

                    // 查询替换车服务单
                    ReplaceVehicleDTO replaceVehicleDTO = MainServeUtil.getReplaceVehicleDTOBySourceServNo(maintenanceAggregateRootApi, cmd.getServeNo());
                    if (Optional.ofNullable(replaceVehicleDTO).isPresent()) {
                        Result<ServeDTO> replaceServeDTOResult = serveAggregateRootApi.getServeDtoByServeNo(replaceVehicleDTO.getServeNo());
                        ServeDTO replaceServe = ResultDataUtils.getInstance(replaceServeDTOResult).getDataOrException();

                        ServeAdjustQry qry = new ServeAdjustQry();
                        qry.setServeNo(replaceServe.getServeNo());
                        Result<ServeAdjustDTO> serveAdjustDTOResult = serveAggregateRootApi.getServeAdjust(qry);

                        ServeAdjustDTO serveAdjustDTO = ResultDataUtils.getInstance(serveAdjustDTOResult).getDataOrException();

                        // 替换单已发车且变更为正常服务单
                        if (serveAdjustDTO != null && Optional.ofNullable(replaceServe)
                                .filter(o -> ServeEnum.DELIVER.getCode().equals(o.getStatus())
                                        && JudgeEnum.YES.getCode().equals(o.getReplaceFlag())).isPresent()) {

                            // 替换车开始计费
                            Result<DeliverDTO> replaceDeliverResult = deliverAggregateRootApi.getDeliverByServeNo(replaceServe.getServeNo());
                            DeliverDTO replaceDeliver = ResultDataUtils.getInstance(replaceDeliverResult).getDataOrException();

                            RenewalCmd renewalCmd = new RenewalCmd();
                            renewalCmd.setServeNo(replaceServe.getServeNo());
                            renewalCmd.setDeliverNo(replaceDeliver.getDeliverNo());
                            renewalCmd.setVehicleId(replaceDeliver.getCarId());
                            renewalCmd.setCustomerId(replaceServe.getCustomerId());
                            renewalCmd.setRent(serveAdjustDTO.getChargeRentAmount());
                            renewalCmd.setRentRatio(serveAdjustDTO.getChargeRentRatio().doubleValue());
                            renewalCmd.setCreateId(cmd.getOperatorId());
                            renewalCmd.setRentEffectDate(FormatUtil.ymdFormatDateToString(FormatUtil.addDays(cmd.getRecoverVehicleTime(), 1)));
                            renewalCmd.setEffectFlag(true);
                            renewalCmd.setVehicleBusinessMode(replaceDeliver.getVehicleBusinessMode());
                            //todo 增加具体业务类型
                            renewalCmd.setBusinessType(replaceServe.getBusinessType());

                            mqTools.send(event, "price_change", null, JSON.toJSONString(renewalCmd));

                            // 服务单调整工单状态改为开始计费并记录开始计费时间
                            log.info("原车收车 替换单调整工单开始计费操作--------------");
                            ServeAdjustStartBillingCmd startBillingCmd = ServeAdjustStartBillingCmd.builder()
                                    .serveNo(replaceServe.getServeNo())
                                    .deliverNo(replaceDeliver.getDeliverNo())
                                    .startBillingDate(FormatUtil.addDays(cmd.getRecoverVehicleTime(), 1)).build();
                            startBillingCmd.setOperatorId(cmd.getOperatorId());

                            serveAggregateRootApi.serveAdjustStartBilling(startBillingCmd);

                            // 修改replace_vehicle的租金为原车租金,租金比例改为原车租金比例
                            replaceVehicleDTO.setRent(serveAdjustDTO.getChargeRentAmount());
                            replaceVehicleDTO.setRentRatio(serveAdjustDTO.getChargeRentRatio());
                            maintenanceAggregateRootApi.updateReplaceVehicle(replaceVehicleDTO);
                        }
                    }
                }
            }
        } else {
            // 发送自动续约消息
            RenewalChargeCmd renewalChargeCmd = new RenewalChargeCmd();
            renewalChargeCmd.setServeNo(cmd.getServeNo());
            renewalChargeCmd.setCreateId(cmd.getOperatorId());
            renewalChargeCmd.setCustomerId(serveDTO.getCustomerId());
            renewalChargeCmd.setDeliverNo(cmd.getDeliverNo());
            renewalChargeCmd.setVehicleId(cmd.getCarId());
            renewalChargeCmd.setEffectFlag(false);
            renewalChargeCmd.setBusinessType(serveDTO.getBusinessType());
            // 续约目标日期为实际收车日期
            renewalChargeCmd.setRenewalDate(DateUtil.formatDate(recoverVehicleTime));
            mqTools.send(event, "renewal_fee", null, JSON.toJSONString(renewalChargeCmd));
        }
    }

    private void updateVehicle(RecoverVehicleCmd cmd) {
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setId(Collections.singletonList(cmd.getCarId()));
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.UNCHECKED.getCode());
        vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.IN.getCode());
        vehicleSaveCmd.setWarehouseId(cmd.getWareHouseId());
        vehicleSaveCmd.setCustomerId(0);
        vehicleSaveCmd.setRecoverDeliverFlag(2);
        Result<WarehouseDto> wareHouseResult = warehouseAggregateRootApi.getWarehouseById(vehicleSaveCmd.getWarehouseId());

        if (ResultDataUtils.getInstance(wareHouseResult).getDataOrException() != null) {
            vehicleSaveCmd.setAddress(wareHouseResult.getData().getName());
        }
        Result<String> changeVehicleStatusResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if (!ResultValidUtils.checkResult(changeVehicleStatusResult)) {
            log.error("收车时修改车辆状态失败，失败信息：{}", changeVehicleStatusResult.getMsg());
        }
    }

    private void recoverCheck(RecoverVehicleCmd cmd) {
        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(cmd.getServeNo());
        DeliverDTO deliverDTO = ResultDataUtils.getInstance(deliverDTOResult).getDataOrException();

        if (DeliverContractStatusEnum.NOSIGN.getCode() != deliverDTO.getRecoverContractStatus()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "选中车辆存在电子交接单，请回列表页查看");
        }
        cmd.setCarId(deliverDTO.getCarId());
    }

    private void checkDate(RecoverVehicleCmd cmd) {
        Date recoverVehicleTime = cmd.getRecoverVehicleTime();
        Date tSubNDate = DateUtils.addDate(DateUtil.parseDate(DateUtil.now()), -DeliverProjectProperties.RECOVER_TIME_RANGE.getPre());
        Date tAddNDate = DateUtils.addDate(DateUtil.parseDate(DateUtil.now()), DeliverProjectProperties.RECOVER_TIME_RANGE.getSuf());
        if (recoverVehicleTime.before(tSubNDate) ||
                recoverVehicleTime.after(tAddNDate)) {
            throw new CommonException(ResultErrorEnum.VILAD_ERROR.getCode(), "收车日期超出可选范围");
        }

        // 查询完成的维修单
        Result<MaintenanceDTO> maintenanceByServeNo = maintenanceAggregateRootApi.getMaintenanceByServeNo(cmd.getServeNo());
        if (ResultValidUtils.checkResult(maintenanceByServeNo)) {
            if (cmd.getRecoverVehicleTime().before(maintenanceByServeNo.getData().getConfirmDate())) {
                log.info("收车日期超出可选范围  参数:{}", cmd);
                throw new CommonException(ResultErrorEnum.VILAD_ERROR.getCode(), "收车日期不可小于维修交车日期");
            }
        }

        Result<ReplaceVehicleDTO> replaceVehicleResult = maintenanceAggregateRootApi.getReplaceVehicleByServeNo(cmd.getServeNo());
        ReplaceVehicleDTO replaceVehicleDTO = ResultDataUtils.getInstance(replaceVehicleResult).getDataOrNull();
        if (null != replaceVehicleDTO) {
            // 查找替换车发车信息
            Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(replaceVehicleDTO.getServeNo());
            if (!Optional.ofNullable(deliverDTOResult).map(Result::getData).isPresent()) {
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "未查询到交付单");
            }
            Result<DeliverVehicleDTO> deliverVehicleDTOResult = deliverVehicleAggregateRootApi.getDeliverVehicleDto(deliverDTOResult.getData().getDeliverNo());
            if (!Optional.ofNullable(deliverVehicleDTOResult).map(Result::getData).isPresent()) {
                throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "未查询到发车单");
            }
            if (cmd.getRecoverVehicleTime().before(deliverVehicleDTOResult.getData().getDeliverVehicleTime())) {
                throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "收车日期小于替换车发车日期");
            }
        }

        Result<DeliverVehicleDTO> deliverVehicleDto = deliverVehicleAggregateRootApi.getDeliverVehicleDto(cmd.getDeliverNo());
        if (ResultValidUtils.checkResult(deliverVehicleDto)) {
            if (cmd.getRecoverVehicleTime().before(deliverVehicleDto.getData().getDeliverVehicleTime())) {
                log.info("收车日期超出可选范围  参数:{}", cmd);
                throw new CommonException(ResultErrorEnum.VILAD_ERROR.getCode(), "收车日期不可小于发车日期");
            }
        }
    }

}
