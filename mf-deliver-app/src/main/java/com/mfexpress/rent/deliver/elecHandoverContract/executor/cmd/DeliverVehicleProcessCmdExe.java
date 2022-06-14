package com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.component.starter.tools.mq.MqTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.DailyAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.daily.CreateDailyCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.cmd.DeliverVehicleProcessCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverImgInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidStockStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeliverVehicleProcessCmdExe {

    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private DailyAggregateRootApi dailyAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource(name = "serveSyncServiceImpl")
    private EsSyncHandlerI serveSyncServiceI;

    @Resource
    private MqTools mqTools;

    @Value("${rocketmq.listenEventTopic}")
    private String event;

    public void execute(DeliverVehicleProcessCmd cmd) {

        ElecContractDTO contractDTO = cmd.getContractDTO();

        // 数据收集
        List<DeliverImgInfo> deliverImgInfos = JSONUtil.toList(contractDTO.getPlateNumberWithImgs(), DeliverImgInfo.class);
        List<String> serveNoList = deliverImgInfos.stream().map(DeliverImgInfo::getServeNo).collect(Collectors.toList());
        Result<List<ServeDTO>> serveDTOListResult = serveAggregateRootApi.getServeDTOByServeNoList(serveNoList);
        List<ServeDTO> serveDTOList = ResultDataUtils.getInstance(serveDTOListResult).getDataOrNull();
        if (CollectionUtils.isEmpty(serveDTOList)) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "服务单信息不存在");
        }

        Map<String, ServeDTO> serveDTOMap = serveDTOList.stream().collect(Collectors.toMap(ServeDTO::getServeNo, Function.identity(), (v1, v2) -> v1));
        //每个服务单对应的预计收车日期
        Map<String, String> expectRecoverDateMap = new HashMap<>(serveDTOList.size());
        for (String serveNo : serveNoList) {
            ServeDTO serve = serveDTOMap.get(serveNo);
            //替换车使用维修车的预计收车日期，重新激活的服务单不更新预计收车日期
            if (!JudgeEnum.YES.getCode().equals(serve.getReplaceFlag()) && !JudgeEnum.YES.getCode().equals(serve.getReactiveFlag())) {
                String expectRecoverDate = getExpectRecoverDate(contractDTO.getDeliverVehicleTime(), serve.getLeaseMonths(), serve.getLeaseDays());
                expectRecoverDateMap.put(serveNo, expectRecoverDate);
            }
        }
        contractDTO.setExpectRecoverDateMap(expectRecoverDateMap);
        //生成发车单 交付单状态更新已发车并初始化操作状态  服务单状态更新为已发车
        Result<Integer> result = deliverVehicleAggregateRootApi.deliverVehicles(contractDTO);
        ResultValidUtils.checkResultException(result);
        List<Integer> carIdList = new LinkedList<>();
        deliverImgInfos.forEach(deliverImgInfo -> {
            carIdList.add(deliverImgInfo.getCarId());
            //发车操作mq触发计费
            ServeDTO serve = serveDTOMap.get(deliverImgInfo.getServeNo());
            com.mfexpress.billing.rentcharge.dto.data.deliver.DeliverVehicleCmd rentChargeCmd = new com.mfexpress.billing.rentcharge.dto.data.deliver.DeliverVehicleCmd();
            rentChargeCmd.setServeNo(deliverImgInfo.getServeNo());
            rentChargeCmd.setDeliverNo(deliverImgInfo.getDeliverNo());
            rentChargeCmd.setRent(serve.getRent());
            String expectRecoverDate = expectRecoverDateMap.get(deliverImgInfo.getServeNo());
            if (Objects.isNull(expectRecoverDate)) {
                //替换车使用原车的预计收车日期作为计费截止日期，重新激活服务单使用原来的预计收车日期作为计费截止日期
                rentChargeCmd.setExpectRecoverDate(serve.getExpectRecoverDate());
            } else {
                rentChargeCmd.setExpectRecoverDate(expectRecoverDate);
            }
            rentChargeCmd.setDeliverFlag(true);
            rentChargeCmd.setCustomerId(serve.getCustomerId());
            rentChargeCmd.setCreateId(contractDTO.getCreatorId());
            rentChargeCmd.setVehicleId(deliverImgInfo.getCarId());
            rentChargeCmd.setDeliverDate(DateUtil.formatDate(contractDTO.getDeliverVehicleTime()));
            mqTools.send(event, "deliver_vehicle", null, JSON.toJSONString(rentChargeCmd));
        });

        // 修改对应的车辆状态为租赁状态
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.OUT.getCode());
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.LEASE.getCode());
        vehicleSaveCmd.setId(carIdList);
        vehicleSaveCmd.setCustomerId(cmd.getCustomerId());
        Result<CustomerVO> customerResult = customerAggregateRootApi.getById(cmd.getCustomerId());
        if (customerResult.getData() != null) {
            vehicleSaveCmd.setAddress(customerResult.getData().getName());
        }
        Result<String> vehicleResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if (ResultErrorEnum.SUCCESSED.getCode() != vehicleResult.getCode()) {
            log.error("发车电子合同签署完成时，更改车辆状态失败。车辆id：{}", carIdList);
        }
        //生成日报
        CreateDailyCmd createDailyCmd = new CreateDailyCmd();
        createDailyCmd.setDeliverFlag(1);
        createDailyCmd.setDeliverRecoverDate(contractDTO.getDeliverVehicleTime());
        createDailyCmd.setServeNoList(serveNoList);

        ResultValidUtils.checkResultException(dailyAggregateRootApi.createDaily(createDailyCmd));

        //同步
        Map<String, String> map = new HashMap<>();
        serveNoList.forEach(s -> {
            map.put("serve_no", s);
            serveSyncServiceI.execOne(map);
        });
    }

    public DeliverVehicleProcessCmd turnToCmd(DeliverDTO deliverDTO, ElecContractDTO elecContractDTO) {

        DeliverVehicleProcessCmd deliverVehicleProcessCmd = new DeliverVehicleProcessCmd();
        deliverVehicleProcessCmd.setCustomerId(deliverDTO.getCustomerId());
        elecContractDTO.setContractForeignNo(elecContractDTO.getContractShowNo());
        deliverVehicleProcessCmd.setContractDTO(elecContractDTO);

        return deliverVehicleProcessCmd;
    }

    private String getExpectRecoverDate(Date deliverVehicleDate, Integer offsetMonths, Integer offsetDays) {
        DateTime dateTime = DateUtil.endOfMonth(deliverVehicleDate);
        String deliverDate = DateUtil.formatDate(deliverVehicleDate);
        String endDate = DateUtil.formatDate(dateTime);
        if (deliverDate.equals(endDate)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateUtil.endOfMonth(dateTime));
            if (null != offsetMonths) {
                calendar.add(Calendar.MONTH, offsetMonths);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
            if (null != offsetDays) {
                // offsetDays -= 1;
                if (offsetDays > 0) {
                    calendar.add(Calendar.DAY_OF_MONTH, offsetDays);
                }
            }
            return DateUtil.formatDate(calendar.getTime());
        } else {
            if (null != offsetMonths) {
                deliverVehicleDate = DateUtil.offsetMonth(deliverVehicleDate, offsetMonths);
            }
            if (null != offsetDays) {
                deliverVehicleDate = DateUtil.offsetDay(deliverVehicleDate, offsetDays);
            }
            return DateUtil.formatDate(deliverVehicleDate);
        }
    }
}
