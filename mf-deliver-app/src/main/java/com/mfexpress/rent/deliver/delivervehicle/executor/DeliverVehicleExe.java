package com.mfexpress.rent.deliver.delivervehicle.executor;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.MqTools;
import com.mfexpress.rent.deliver.api.SyncServiceI;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleImgCmd;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidStockStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DeliverVehicleExe {

    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;
    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;
    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;
    @Resource
    private SyncServiceI syncServiceI;

    @Resource
    private MqTools mqTools;
    @Value("${rocketmq.listenEventTopic}")
    private String event;

    public String execute(DeliverVehicleCmd deliverVehicleCmd) {
        //生成发车单 交付单状态更新已发车 初始化操作状态  服务单状态更新为已发车  调用车辆服务为租赁状态
        List<DeliverVehicleImgCmd> deliverVehicleImgCmdList = deliverVehicleCmd.getDeliverVehicleImgCmdList();
        List<DeliverVehicleDTO> deliverVehicleDTOList = new LinkedList<>();
        //List<DailyDTO> dailyDTOList = new LinkedList<>();
        //更新服务单状态

        List<String> serveNoList = deliverVehicleImgCmdList.stream().map(DeliverVehicleImgCmd::getServeNo).collect(Collectors.toList());
        List<Integer> carIdList = deliverVehicleImgCmdList.stream().map(DeliverVehicleImgCmd::getCarId).collect(Collectors.toList());
        //触发计费
        Result<Map<String, Serve>> serveMapResult = serveAggregateRootApi.getServeMapByServeNoList(serveNoList);
        if (serveMapResult.getCode() != 0) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "服务单信息不存在");
        }
        Map<String, Serve> serveMap = serveMapResult.getData();
        for (DeliverVehicleImgCmd deliverVehicleImgCmd : deliverVehicleImgCmdList) {
            DeliverVehicleDTO deliverVehicleDTO = new DeliverVehicleDTO();
            deliverVehicleDTO.setServeNo(deliverVehicleImgCmd.getServeNo());
            deliverVehicleDTO.setDeliverNo(deliverVehicleImgCmd.getDeliverNo());
            deliverVehicleDTO.setImgUrl(deliverVehicleImgCmd.getImgUrl());
            deliverVehicleDTO.setContactsName(deliverVehicleCmd.getContactsName());
            deliverVehicleDTO.setContactsPhone(deliverVehicleCmd.getContactsPhone());
            deliverVehicleDTO.setContactsCard(deliverVehicleCmd.getContactsCard());
            deliverVehicleDTO.setDeliverVehicleTime(deliverVehicleCmd.getDeliverVehicleTime());
            deliverVehicleDTOList.add(deliverVehicleDTO);

            Serve serve = serveMap.get(deliverVehicleImgCmd.getServeNo());

            com.mfexpress.billing.rentcharge.dto.data.deliver.DeliverVehicleCmd rentChargeCmd = new com.mfexpress.billing.rentcharge.dto.data.deliver.DeliverVehicleCmd();
            rentChargeCmd.setServeNo(serve.getServeNo());
            rentChargeCmd.setDeliverNo(deliverVehicleImgCmd.getDeliverNo());
            rentChargeCmd.setRent(serve.getRent());

            rentChargeCmd.setDeliverDate(DateUtil.formatDate(deliverVehicleCmd.getDeliverVehicleTime()));
            //替换车
            if (serve.getReplaceFlag().equals(1)){
                rentChargeCmd.setExpectRecoverDate(serve.getLeaseEndDate());
            }else {
                rentChargeCmd.setExpectRecoverDate(getExpectRecoverDate(deliverVehicleCmd.getDeliverVehicleTime(),serve.getLeaseMonths()));
            }
            rentChargeCmd.setDeliverFlag(true);
            rentChargeCmd.setCustomerId(deliverVehicleCmd.getCustomerId());
            rentChargeCmd.setCreateId(deliverVehicleCmd.getCarServiceId());
            rentChargeCmd.setVehicleId(deliverVehicleImgCmd.getCarId());
            //发车操作mq触发计费
            mqTools.send(event, "deliver_vehicle", null, JSON.toJSONString(rentChargeCmd));
        }

        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.OUT.getCode());
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.LEASE.getCode());
        vehicleSaveCmd.setId(carIdList);
        vehicleSaveCmd.setCustomerId(deliverVehicleCmd.getCustomerId());
        Result<CustomerVO> customerResult = customerAggregateRootApi.getById(deliverVehicleCmd.getCustomerId());
        if (customerResult.getData() != null) {
            vehicleSaveCmd.setAddress(customerResult.getData().getName());
        }
        Result<String> vehicleResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if (vehicleResult.getCode() != 0) {
            return vehicleResult.getMsg();
        }
        //服务单 更新状态为发车
        Result<String> serveResult = serveAggregateRootApi.deliver(serveNoList);
        if (serveResult.getCode() != 0) {
            return serveResult.getMsg();
        }
        // 交付单 更新状态为已发车
        Result<String> deliverResult = deliverAggregateRootApi.toDeliver(serveNoList);
        if (deliverResult.getCode() != 0) {
            return deliverResult.getMsg();
        }
        DeliverCarServiceDTO deliverCarServiceDTO = new DeliverCarServiceDTO();
        deliverCarServiceDTO.setCarServiceId(deliverVehicleCmd.getCarServiceId());
        deliverCarServiceDTO.setServeNoList(serveNoList);
        deliverAggregateRootApi.saveCarServiceId(deliverCarServiceDTO);
        Result<String> deliverVehicleResult = deliverVehicleAggregateRootApi.addDeliverVehicle(deliverVehicleDTOList);

        for (String serveNo : serveNoList) {
            syncServiceI.execOne(serveNo);
        }
        return deliverVehicleResult.getData();
    }

    private  String getExpectRecoverDate(Date deliverVehicleDate, int offset) {
        DateTime dateTime = DateUtil.endOfMonth(deliverVehicleDate);
        String deliverDate = DateUtil.formatDate(deliverVehicleDate);
        String endDate = DateUtil.formatDate(dateTime);
        if (deliverDate.equals(endDate)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateTime);
            calendar.add(Calendar.MONTH, offset);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            return DateUtil.formatDate(calendar.getTime());
        } else {
            return DateUtil.formatDate(DateUtil.offsetMonth(deliverVehicleDate, offset));
        }
    }


}

