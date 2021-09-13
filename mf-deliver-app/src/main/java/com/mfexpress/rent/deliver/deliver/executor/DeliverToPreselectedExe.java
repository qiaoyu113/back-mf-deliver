package com.mfexpress.rent.deliver.deliver.executor;


import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverPreselectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverVehicleSelectCmd;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Component
public class DeliverToPreselectedExe {


    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;


    public String toPreselected(DeliverPreselectedCmd deliverPreselectedCmd) {
        List<DeliverDTO> deliverList = new LinkedList<>();
        //服务单编号
        List<String> serveNoList = deliverPreselectedCmd.getServeList();
        //车辆信息
        List<DeliverVehicleSelectCmd> deliverVehicleSelectCmdList = deliverPreselectedCmd.getDeliverVehicleSelectCmdList();
        //车辆id list
        List<Integer> carIdList = new LinkedList<>();
        for (int i = 0; i < serveNoList.size(); i++) {

            DeliverDTO deliverDTO = new DeliverDTO();
            DeliverVehicleSelectCmd deliverVehicleSelectCmd;
            try {
                deliverVehicleSelectCmd = deliverVehicleSelectCmdList.get(i);
            } catch (IndexOutOfBoundsException e) {
                throw new RuntimeException("批量预选数量错误");
            }
            //todo 查看车辆状态 是否已投保若已投保更新投保状态

            deliverDTO.setServeNo(serveNoList.get(i));
            deliverDTO.setCarId(deliverVehicleSelectCmd.getCarId());
            deliverDTO.setCarNum(deliverVehicleSelectCmd.getCarNum());
            deliverDTO.setDeliverStatus(DeliverEnum.IS_DELIVER.getCode());
            deliverDTO.setStatus(ValidStatusEnum.VALID.getCode());
            deliverDTO.setFrameNum(deliverVehicleSelectCmd.getFrameNum());
            deliverDTO.setMileage(deliverVehicleSelectCmd.getMileage());
            deliverDTO.setVehicleAge(deliverVehicleSelectCmd.getVehicleAge());
            deliverList.add(deliverDTO);
            carIdList.add(deliverVehicleSelectCmd.getCarId());

        }

        //todo 更新车辆已预选状态

        serveAggregateRootApi.toPreselected(serveNoList);

        deliverAggregateRootApi.addDeliver(deliverList);


        return "";
    }

}
