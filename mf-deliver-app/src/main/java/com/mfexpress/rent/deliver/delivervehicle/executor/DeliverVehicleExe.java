package com.mfexpress.rent.deliver.delivervehicle.executor;

import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleDTO;

import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleImgCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Component
public class DeliverVehicleExe {

    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;
    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    public String deliver(DeliverVehicleCmd deliverVehicleCmd) {
        //todo 生成发车单 交付单状态更新已发车 初始化操作状态  服务单状态更新为已发车  调用车辆服务为租赁状态
        List<DeliverVehicleImgCmd> deliverVehicleImgCmdList = deliverVehicleCmd.getDeliverVehicleImgCmdList();
        List<DeliverVehicleDTO> deliverVehicleDTOList = new LinkedList<>();
        //更新服务单状态
        List<String> serveNoList = new LinkedList<>();
        List<Integer> carIdList = new LinkedList<>();
        for (DeliverVehicleImgCmd deliverVehicleImgCmd : deliverVehicleImgCmdList) {

            serveNoList.add(deliverVehicleImgCmd.getServeNo());

            carIdList.add(deliverVehicleImgCmd.getCarId());

            DeliverVehicleDTO deliverVehicleDTO = new DeliverVehicleDTO();
            deliverVehicleDTO.setServeNo(deliverVehicleImgCmd.getServeNo());
            deliverVehicleDTO.setDeliverNo(deliverVehicleImgCmd.getDeliverNo());
            deliverVehicleDTO.setImgUrl(deliverVehicleImgCmd.getImgUrl());
            deliverVehicleDTO.setContactsName(deliverVehicleCmd.getContactsName());
            deliverVehicleDTO.setContactsPhone(deliverVehicleCmd.getContactsPhone());
            deliverVehicleDTO.setContactsCard(deliverVehicleCmd.getContactsCard());
            deliverVehicleDTO.setDeliverVehicleTime(deliverVehicleCmd.getDeliverVehicleTime());
            deliverVehicleDTOList.add(deliverVehicleDTO);
        }

        //todo 更新车辆状态

        //服务单 更新状态为发车
        serveAggregateRootApi.deliver(serveNoList);
        // 交付单 更新状态为已发车 todo  初始化操作状态
        deliverAggregateRootApi.toDeliver(serveNoList);
        deliverVehicleAggregateRootApi.addDeliverVehicle(deliverVehicleDTOList);

        return "";
    }
}
