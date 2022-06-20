package com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.billing.rentcharge.dto.data.deliver.RecoverVehicleCmd;
import com.mfexpress.billing.rentcharge.dto.data.deliver.RenewalCmd;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.component.starter.tools.mq.MqTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.api.ServeServiceI;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.LeaseModelEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.DailyAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.daily.CreateDailyCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.cmd.RecoverVehicleProcessCmd;
import com.mfexpress.rent.deliver.dto.data.serve.RenewalChargeCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.cmd.ServeAdjustStartBillingCmd;
import com.mfexpress.rent.deliver.utils.FormatUtil;
import com.mfexpress.rent.deliver.utils.MainServeUtil;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.constant.MaintenanceTypeEnum;
import com.mfexpress.rent.maintain.dto.data.MaintenanceDTO;
import com.mfexpress.rent.maintain.dto.data.ReplaceVehicleDTO;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.WarehouseAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidStockStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.rent.vehicle.data.dto.warehouse.WarehouseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecoverVehicleProcessCmdExe {

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private WarehouseAggregateRootApi warehouseAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private MaintenanceAggregateRootApi maintenanceAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private DailyAggregateRootApi dailyAggregateRootApi;

    @Resource
    private ServeServiceI serveServiceI;

    private MqTools mqTools;

    @Value("${rocketmq.listenEventTopic}")
    private String event;

    @Resource(name = "serveSyncServiceImpl")
    private EsSyncHandlerI serveSyncServiceI;

    @Resource
    private BeanFactory beanFactory;

    public void execute(RecoverVehicleProcessCmd cmd) {

        if (null == mqTools) {
            mqTools = beanFactory.getBean(MqTools.class);
        }

        List<String> serveNoList = new LinkedList<>();
        // 交付单、服务单修改
        Result<Integer> recoveredResult = recoverVehicleAggregateRootApi.recovered(cmd.getDeliverNo(), cmd.getContractForeignNo());
        ResultValidUtils.checkResultException(recoveredResult);

        //更新车辆状态
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setId(Collections.singletonList(cmd.getCarId()));
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.UNCHECKED.getCode());
        vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.IN.getCode());
        vehicleSaveCmd.setWarehouseId(cmd.getRecoverWareHouseId());
        vehicleSaveCmd.setCustomerId(0);
        Result<WarehouseDto> wareHouseResult = warehouseAggregateRootApi.getWarehouseById(vehicleSaveCmd.getWarehouseId());

        if (ResultDataUtils.getInstance(wareHouseResult).getDataOrException() != null) {
            vehicleSaveCmd.setAddress(wareHouseResult.getData().getName());
        }
        Result<String> changeVehicleStatusResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);

        ResultValidUtils.checkResultException(changeVehicleStatusResult);

        // 判断实际收车日期和预计收车日期的前后关系，如果实际收车日期在预计收车日期之前或当天，发送收车计费消息，反之，发送自动续约消息

        Date recoverVehicleTime = cmd.getRecoverVehicleTime();
        String expectRecoverDateChar = cmd.getExpectRecoverDate();
        DateTime expectRecoverDate = DateUtil.parseDate(expectRecoverDateChar);
        // 发送收车计费消息
        if (expectRecoverDate.isAfterOrEquals(recoverVehicleTime)) {
            //收车计费
            RecoverVehicleCmd recoverVehicleCmd = new RecoverVehicleCmd();
            recoverVehicleCmd.setServeNo(cmd.getServeNo());
            recoverVehicleCmd.setVehicleId(cmd.getCarId());
            recoverVehicleCmd.setDeliverNo(cmd.getDeliverNo());
            recoverVehicleCmd.setCustomerId(cmd.getCustomerId());
            recoverVehicleCmd.setCreateId(cmd.getOperatorId());
            recoverVehicleCmd.setRecoverDate(DateUtil.formatDate(cmd.getRecoverVehicleTime()));
            log.info("正常收车时，交付域向计费域发送的收车单信息：{}", recoverVehicleCmd);
            mqTools.send(event, "recover_vehicle", null, JSON.toJSONString(recoverVehicleCmd));

            // 服务单维修中
            if (ServeEnum.REPAIR.getCode().equals(cmd.getServeStatus())) {
                // 查找维修单
                MaintenanceDTO maintenanceDTO = MainServeUtil.getMaintenanceByServeNo(maintenanceAggregateRootApi, cmd.getServeNo());
                // 维修性质为故障维修
                if (MaintenanceTypeEnum.FAULT.getCode().intValue() == maintenanceDTO.getType()) {
                    // 原车维修单变为库存中维修
                    ResultValidUtils.checkResultException(maintenanceAggregateRootApi.updateMaintenanceDetailByServeNo(cmd.getServeNo()));

                    // 查询替换车服务单
                    ReplaceVehicleDTO replaceVehicleDTO = MainServeUtil.getReplaceVehicleDTOBySourceServNo(maintenanceAggregateRootApi, cmd.getServeNo());
                    if (Optional.ofNullable(replaceVehicleDTO).isPresent()) {
                        Result<ServeDTO> replaceServeDTOResult = serveAggregateRootApi.getServeDtoByServeNo(replaceVehicleDTO.getServeNo());
                        ServeDTO replaceServe = ResultDataUtils.getInstance(replaceServeDTOResult).getDataOrException();
                        // 替换单已发车且变更为正常服务单
                        if (Optional.ofNullable(replaceServe)
                                .filter(o -> ServeEnum.DELIVER.getCode().equals(o.getStatus())
                                        && JudgeEnum.NO.getCode().equals(o.getReplaceFlag())).isPresent()) {

                            // 替换车开始计费
                            Result<DeliverDTO> replaceDeliverResult = deliverAggregateRootApi.getDeliverByServeNo(replaceServe.getServeNo());
                            DeliverDTO replaceDeliver = ResultDataUtils.getInstance(replaceDeliverResult).getDataOrException();
                            RenewalCmd renewalCmd = new RenewalCmd();
                            renewalCmd.setServeNo(replaceServe.getServeNo());
                            renewalCmd.setDeliverNo(replaceDeliver.getDeliverNo());
                            renewalCmd.setVehicleId(replaceDeliver.getCarId());
                            renewalCmd.setCustomerId(replaceServe.getCustomerId());
                            renewalCmd.setRent(replaceServe.getRent());
                            renewalCmd.setRentRatio(replaceServe.getRentRatio().doubleValue());
                            renewalCmd.setCreateId(cmd.getOperatorId());
                            renewalCmd.setRentEffectDate(FormatUtil.ymdFormatDateToString(FormatUtil.addDays(cmd.getRecoverVehicleTime(), 1)));
                            renewalCmd.setEffectFlag(true);
                            mqTools.send(event, "price_change", null, JSON.toJSONString(renewalCmd));

                            // 服务单调整工单状态改为开始计费并记录开始计费时间
                            ServeAdjustStartBillingCmd startBillingCmd = ServeAdjustStartBillingCmd.builder()
                                    .serveNo(replaceServe.getServeNo())
                                    .deliverNo(replaceDeliver.getDeliverNo())
                                    .startBillingDate(FormatUtil.addDays(cmd.getRecoverVehicleTime(), 1)).build();

                            serveAggregateRootApi.serveAdjustStartBilling(startBillingCmd);
                        }
                    }
                }
            }
        } else {
            // 发送自动续约消息
            RenewalChargeCmd renewalChargeCmd = new RenewalChargeCmd();
            renewalChargeCmd.setServeNo(cmd.getServeNo());
            renewalChargeCmd.setCreateId(cmd.getOperatorId());
            renewalChargeCmd.setCustomerId(cmd.getCustomerId());
            renewalChargeCmd.setDeliverNo(cmd.getDeliverNo());
            renewalChargeCmd.setVehicleId(cmd.getCarId());
            renewalChargeCmd.setEffectFlag(false);
            // 续约目标日期为实际收车日期
            renewalChargeCmd.setRenewalDate(DateUtil.formatDate(recoverVehicleTime));
            mqTools.send(event, "renewal_fee", null, JSON.toJSONString(renewalChargeCmd));
        }

        serveNoList.add(cmd.getServeNo());

        CreateDailyCmd createDailyCmd = new CreateDailyCmd();
        createDailyCmd.setDeliverFlag(0);
        createDailyCmd.setDeliverRecoverDate(cmd.getRecoverVehicleTime());
        createDailyCmd.setServeNoList(serveNoList);

        ResultValidUtils.checkResultException(dailyAggregateRootApi.createDaily(createDailyCmd));

        //同步
        Map<String, String> map = new HashMap<>();
        serveNoList.forEach(serveNo -> {
            map.put("serve_no", serveNo);
            serveSyncServiceI.execOne(map);
        });
    }

    public RecoverVehicleProcessCmd turnToCmd(ElecContractDTO contractDTO, DeliverDTO deliverDTO, ServeDTO serveDTO) {

        RecoverVehicleProcessCmd cmd = new RecoverVehicleProcessCmd();
        cmd.setContractForeignNo(contractDTO.getContractForeignNo());
        cmd.setRecoverVehicleTime(contractDTO.getRecoverVehicleTime());
        cmd.setCarId(deliverDTO.getCarId());
        cmd.setServeNo(serveDTO.getServeNo());
        cmd.setDeliverNo(deliverDTO.getDeliverNo());
        cmd.setCustomerId(serveDTO.getCustomerId());
        cmd.setExpectRecoverDate(serveDTO.getExpectRecoverDate());
        cmd.setRecoverWareHouseId(contractDTO.getRecoverWareHouseId());
        cmd.setContactId(contractDTO.getContractId());
        cmd.setServeStatus(serveDTO.getStatus());
        cmd.setOperatorId(contractDTO.getCreatorId());

        return cmd;
    }
}
