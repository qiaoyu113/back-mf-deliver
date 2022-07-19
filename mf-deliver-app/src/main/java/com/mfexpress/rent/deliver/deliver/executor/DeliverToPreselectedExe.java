package com.mfexpress.rent.deliver.deliver.executor;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.constant.DeliverEnum;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.constant.ValidStatusEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverPreselectedCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverVehicleSelectCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCheckCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.entity.Serve;
import com.mfexpress.rent.deliver.serve.executor.ReactiveServeCheckCmdExe;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.VehicleInsuranceAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInfoDto;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.rent.vehicle.data.dto.vehicleinsurance.VehicleInsuranceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j
public class DeliverToPreselectedExe {
    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;
    @Resource(name = "serveSyncServiceImpl")
    private EsSyncHandlerI syncServiceI;

    @Resource
    private VehicleInsuranceAggregateRootApi vehicleInsuranceAggregateRootApi;

    @Resource
    private ReactiveServeCheckCmdExe reactiveServeCheck;


    public String execute(DeliverPreselectedCmd deliverPreselectedCmd) {
        List<DeliverDTO> deliverList = new LinkedList<>();
        //服务单编号
        List<String> serveNoList = deliverPreselectedCmd.getServeList();
        //车辆信息
        List<DeliverVehicleSelectCmd> deliverVehicleSelectCmdList = deliverPreselectedCmd.getDeliverVehicleSelectCmdList();
        //车辆id list
        List<Integer> carIdList = new LinkedList<>();
        if (serveNoList.size() != deliverVehicleSelectCmdList.size()) {
            log.error("服务单选中数量与预选车辆数量不符");
            throw new CommonException(ResultErrorEnum.VILAD_ERROR.getCode(), ResultErrorEnum.VILAD_ERROR.getName());
        }

        // 重新激活的服务单在进行预选操作时需要的校验
        ReactivateServeCheckCmd reactivateServeCheckCmd = ReactivateServeCheckCmd.builder().serveNoList(deliverPreselectedCmd.getServeList()).build();
        reactiveServeCheck.execute(reactivateServeCheckCmd);

        for (int i = 0; i < serveNoList.size(); i++) {
            DeliverDTO deliverDTO = new DeliverDTO();
            //已经生成交付单 不能重复预选
            deliverDTO.setServeNo(serveNoList.get(i));
            Result<DeliverDTO> deliverResult = deliverAggregateRootApi.getDeliverByServeNo(deliverDTO.getServeNo());
            if (deliverResult.getData() != null) {
                continue;
            }
            DeliverVehicleSelectCmd deliverVehicleSelectCmd = deliverVehicleSelectCmdList.get(i);
            Result<VehicleInfoDto> vehicleResult = vehicleAggregateRootApi.getVehicleInfoVOById(deliverVehicleSelectCmd.getId());
            if (vehicleResult.getCode() != 0 || vehicleResult.getData() == null) {
                throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), vehicleResult.getMsg());
            }
            VehicleInfoDto vehicleInfoDto = vehicleResult.getData();
            if (JudgeEnum.YES.getCode().equals(vehicleInfoDto.getInsuranceStatus())) {
                deliverDTO.setIsInsurance(JudgeEnum.YES.getCode());
                Result<VehicleInsuranceDto> vehicleInsuranceDtoResult = vehicleInsuranceAggregateRootApi.getVehicleInsuranceById(deliverVehicleSelectCmd.getId());
                if (null != vehicleInsuranceDtoResult.getData()) {
                    deliverDTO.setInsuranceStartTime(DeliverUtils.getYYYYMMDDByString(vehicleInsuranceDtoResult.getData().getStartTime()));
                }
            }

            deliverDTO.setCarId(deliverVehicleSelectCmd.getId());
            deliverDTO.setCarNum(deliverVehicleSelectCmd.getPlateNumber());
            deliverDTO.setDeliverStatus(DeliverEnum.IS_DELIVER.getCode());
            deliverDTO.setStatus(ValidStatusEnum.VALID.getCode());
            deliverDTO.setFrameNum(deliverVehicleSelectCmd.getVin());
            deliverDTO.setMileage(deliverVehicleSelectCmd.getMileage());
            deliverDTO.setVehicleAge(deliverVehicleSelectCmd.getVehicleAge());
            deliverDTO.setCustomerId(deliverPreselectedCmd.getCustomerId());
            deliverDTO.setCarServiceId(deliverPreselectedCmd.getCarServiceId());
            deliverDTO.setVehicleBusinessMode(vehicleInfoDto.getVehicleBusinessMode());
            deliverList.add(deliverDTO);
            carIdList.add(deliverVehicleSelectCmd.getId());

        }

        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setId(carIdList);
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.CHECKED.getCode());
        Result<String> vehicleResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if (vehicleResult.getCode() != 0) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), vehicleResult.getMsg());
        }

        Result<String> serveResult = serveAggregateRootApi.toPreselected(serveNoList);
        if (serveResult.getCode() != 0) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), serveResult.getMsg());
        }
        Result<String> deliverResult = deliverAggregateRootApi.addDeliver(deliverList);
        if (deliverResult.getCode() != 0) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), deliverResult.getMsg());
        }
        //强同步es
        HashMap<String, String> map = new HashMap<>();
        for (String serveNo : serveNoList) {
            map.put("serve_no", serveNo);
            syncServiceI.execOne(map);
        }

        return deliverResult.getData();
    }

}
