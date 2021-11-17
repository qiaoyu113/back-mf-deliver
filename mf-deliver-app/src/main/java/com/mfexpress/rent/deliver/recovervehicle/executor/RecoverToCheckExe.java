package com.mfexpress.rent.deliver.recovervehicle.executor;


import cn.hutool.core.date.DateUtil;
import com.mfexpress.billing.rentcharge.api.DailyAggregateRootApi;
import com.mfexpress.billing.rentcharge.api.VehicleDamageAggregateRootApi;
import com.mfexpress.billing.rentcharge.dto.data.VehicleDamage.CreateVehicleDamageCmd;
import com.mfexpress.billing.rentcharge.dto.data.daily.DailyDTO;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.utils.RedisTools;
import com.mfexpress.rent.deliver.api.SyncServiceI;
import com.mfexpress.rent.deliver.constant.Constants;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVechicleCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVehicleDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.WarehouseAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidStockStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleInfoDto;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.rent.vehicle.data.dto.warehouse.WarehouseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j
public class RecoverToCheckExe {

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;
    @Resource
    private SyncServiceI syncServiceI;
    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;
    @Resource
    private WarehouseAggregateRootApi warehouseAggregateRootApi;
    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private DailyAggregateRootApi dailyAggregateRootApi;

    @Resource
    private VehicleDamageAggregateRootApi vehicleDamageAggregateRootApi;

    @Resource
    private RedisTools redisTools;

    public String execute(RecoverVechicleCmd recoverVechicleCmd) {
        //完善收车单信息
        RecoverVehicleDTO recoverVehicleDTO = new RecoverVehicleDTO();
        BeanUtils.copyProperties(recoverVechicleCmd, recoverVehicleDTO);
        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(recoverVechicleCmd.getServeNo());
        if (deliverDTOResult.getData() == null) {
            log.error("不存在交付单，服务单号，{}" + recoverVechicleCmd.getServeNo());
            return ResultErrorEnum.DATA_NOT_FOUND.getName();
        }
        DeliverDTO deliverDTO = deliverDTOResult.getData();
        Result<VehicleInfoDto> vehicleDtoResult = vehicleAggregateRootApi.getVehicleInfoVOById(deliverDTO.getCarId());
        if (vehicleDtoResult == null) {
            log.error("不存在车辆，服务单号，{}" + recoverVechicleCmd.getServeNo());
            return ResultErrorEnum.DATA_NOT_FOUND.getName();
        }
        Result<ServeDTO> serveDTOResult1 = serveAggregateRootApi.getServeDtoByServeNo(recoverVechicleCmd.getServeNo());
        if (serveDTOResult1.getCode() != 0) {
            return "服务单不存在";
        }
        ServeDTO serve = serveDTOResult1.getData();
        if (serve.getStatus().equals(ServeEnum.REPAIR.getCode())) {
            return "服务单维修中不允许收车";
        }
        Result<String> recoverResult = recoverVehicleAggregateRootApi.toCheck(recoverVehicleDTO);
        if (recoverResult.getCode() != 0) {
            return recoverResult.getMsg();
        }
        // 删除缓存中暂存的验车信息
        String serveNo = recoverVechicleCmd.getServeNo();
        String key = DeliverUtils.concatCacheKey(Constants.RECOVER_VEHICLE_CHECK_INFO_CACHE_KEY, serveNo, "*");
        Set<String> keys = redisTools.getKeys(key);
        if(!keys.isEmpty()){
            redisTools.delKeys(new ArrayList<>(keys));
            log.info("收车验车信息保存操作，服务单号为{}的收车验车暂存信息被删除，key分别是:{}", serveNo, keys);
        }

        // 保存费用到计费域
        CreateVehicleDamageCmd cmd = new CreateVehicleDamageCmd();
        cmd.setServeNo(serve.getServeNo());
        cmd.setOrderId(serve.getOrderId());
        cmd.setCustomerId(serve.getCustomerId());
        cmd.setCarNum(deliverDTO.getCarNum());
        cmd.setFrameNum(deliverDTO.getFrameNum());
        cmd.setDamageFee(recoverVechicleCmd.getDamageFee());
        cmd.setParkFee(recoverVechicleCmd.getParkFee());
        Result<Integer> createVehicleDamageResult = vehicleDamageAggregateRootApi.createVehicleDamage(cmd);
        if(createVehicleDamageResult.getCode() != 0){
            // 目前没有分布式事务，如果保存费用失败不应影响后续逻辑的执行
            log.error("收车时验车，保存费用到计费域失败，serveNo：{}", serve.getServeNo());
        }

        //更新交付单状态未 已验车 已收车
        Result<Integer> deliverResult = deliverAggregateRootApi.toCheck(recoverVechicleCmd.getServeNo());
        if (deliverResult.getCode() == 0) {
            //更新车辆状态
            VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
            vehicleSaveCmd.setId(Arrays.asList(deliverResult.getData()));
            vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.UNCHECKED.getCode());
            vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.IN.getCode());
            vehicleSaveCmd.setWarehouseId(recoverVechicleCmd.getWareHouseId());
            Result<WarehouseDto> wareHouseResult = warehouseAggregateRootApi.getWarehouseById(vehicleSaveCmd.getWarehouseId());
            if (wareHouseResult.getData() != null) {
                vehicleSaveCmd.setAddress(wareHouseResult.getData().getName());
            }
            vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        }
        //服务单更新已收车
        Result<String> serveResult = serveAggregateRootApi.recover(Arrays.asList(recoverVechicleCmd.getServeNo()));
        if (serveResult.getCode() != 0) {
            return serveResult.getMsg();
        }

        DeliverCarServiceDTO deliverCarServiceDTO = new DeliverCarServiceDTO();
        deliverCarServiceDTO.setCarServiceId(recoverVechicleCmd.getCarServiceId());
        deliverCarServiceDTO.setServeNoList(Arrays.asList(recoverVechicleCmd.getServeNo()));
        deliverAggregateRootApi.saveCarServiceId(deliverCarServiceDTO);

        //生成收车租赁日报
        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(recoverVechicleCmd.getServeNo());
        if (serveDTOResult.getData() != null) {
            List<DailyDTO> dailyDTOList = new LinkedList<>();
            ServeDTO serveDTO = serveDTOResult.getData();
            DailyDTO dailyDTO = new DailyDTO();
            dailyDTO.setCustomerId(serveDTO.getCustomerId());
            dailyDTO.setStatus(JudgeEnum.YES.getCode());
            dailyDTO.setRentDate(DateUtil.format(recoverVechicleCmd.getRecoverVehicleTime(), "yyyy-MM-dd"));
            dailyDTO.setServeNo(recoverVechicleCmd.getServeNo());
            dailyDTO.setDelFlag(JudgeEnum.NO.getCode());
            dailyDTOList.add(dailyDTO);
            dailyAggregateRootApi.createDaily(dailyDTOList);
        }

        //同步
        syncServiceI.execOne(recoverVechicleCmd.getServeNo());
        return deliverResult.getMsg();
    }
}

