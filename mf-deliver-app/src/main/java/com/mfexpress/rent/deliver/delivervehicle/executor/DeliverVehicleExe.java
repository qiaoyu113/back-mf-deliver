package com.mfexpress.rent.deliver.delivervehicle.executor;

import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.component.starter.tools.mq.MqTools;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.DeliverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.delivervehicle.DeliverVehicleCmd;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
    private EsSyncHandlerI syncServiceI;

    /*@Resource
    private MqTools mqTools;*/

    /*@Value("${rocketmq.listenEventTopic}")
    private String event;*/

    public String execute(DeliverVehicleCmd deliverVehicleCmd) {
        //生成发车单 交付单状态更新已发车 初始化操作状态  服务单状态更新为已发车  调用车辆服务为租赁状态
        /*List<DeliverVehicleImgCmd> deliverVehicleImgCmdList = deliverVehicleCmd.getDeliverVehicleImgCmdList();
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

            //发车操作mq触发计费
            DailyOperate operate = new DailyOperate();
            operate.setServeNo(deliverVehicleImgCmd.getServeNo());
            operate.setCustomerId(deliverVehicleCmd.getCustomerId());
            operate.setOperateDate(DateUtil.formatDate(deliverVehicleCmd.getDeliverVehicleTime()));
            mqTools.send(event, "deliver_vehicle", null, JSON.toJSONString(operate));
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

        HashMap<String, String> map = new HashMap<>();
        for (String serveNo : serveNoList) {
            map.put("serve_no", serveNo);
            syncServiceI.execOne(map);
        }

        return deliverVehicleResult.getData();*/
        return null;
    }
}
