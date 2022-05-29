package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.order.api.app.ContractAggregateRootApi;
import com.mfexpress.order.constant.ContractStatusEnum;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverVechicleCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class RecoverToCheckExe {

    /*@Resource
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
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Resource
    private RedisTools redisTools;
    @Resource
    private MqTools mqTools;
    @Value("${rocketmq.listenEventTopic}")
    private String topic;*/

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private ContractAggregateRootApi contractAggregateRootApi;

    public String execute(RecoverVechicleCmd recoverVechicleCmd) {
        //完善收车单信息
        /*Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(recoverVechicleCmd.getServeNo());
        if (deliverDTOResult.getData() == null) {
            log.error("不存在交付单，服务单号，{}" + recoverVechicleCmd.getServeNo());
            return ResultErrorEnum.DATA_NOT_FOUND.getName();
        }
        DeliverDTO deliverDTO = deliverDTOResult.getData();
        Result<VehicleInfoDto> vehicleDtoResult = vehicleAggregateRootApi.getVehicleInfoVOById(deliverDTO.getCarId());
        if (vehicleDtoResult == null) {
            log.error("不存在车辆，服务单号，{}" + recoverVechicleCmd.getServeNo());
            return ResultErrorEnum.DATA_NOT_FOUND.getName();
        }*/
        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(recoverVechicleCmd.getServeNo());
        if (serveDTOResult.getCode() != 0) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), ResultErrorEnum.OPER_ERROR.getName());
        }
        ServeDTO serve = serveDTOResult.getData();
        /*
            TODO serve.getStatus().equals(ServeEnum.REPAIR.getCode())
                &&服务单的维修状态为维修中
                &&(车辆维修单的维修类型为事故维修||存在未发车的替换车||存在替换车)
         */
        if (serve.getStatus().equals(ServeEnum.REPAIR.getCode())) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "服务单维修中不允许收车");
        }

        Result<Integer> countResult = contractAggregateRootApi.getRenewalContractCountByStatusAndServeNo(ContractStatusEnum.CREATED.getCode(), recoverVechicleCmd.getServeNo());
        Integer count = ResultDataUtils.getInstance(countResult).getDataOrException();
        if(null == count){
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "判断服务单是否已被合同续约失败");
        }
        if(0 != count){
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "该服务单已被合同预续约，不支持收车操作");
        }

        //收车验车时 收车日期不能早于发车日期
        // 2020-11-23 00:00:00.before(2021-11-20 00:00:00)
        /*Result<DeliverVehicleDTO> deliverVehicleDTOResult = deliverVehicleAggregateRootApi.getDeliverVehicleDto(deliverDTO.getDeliverNo());
        if (!ResultStatusEnum.SUCCESSED.getCode().equals(deliverVehicleDTOResult.getCode()) || null == deliverVehicleDTOResult.getData()) {
            throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "发车单查询失败");
        }
        Date deliverVehicleTime = deliverVehicleDTOResult.getData().getDeliverVehicleTime();
        if (!deliverVehicleTime.equals(recoverVechicleCmd.getRecoverVehicleTime())) {
            if (!deliverVehicleTime.before(recoverVechicleCmd.getRecoverVehicleTime())) {
                throw new CommonException(ResultErrorEnum.SERRVER_ERROR.getCode(), "收车日期不能早于发车日期");
            }
        }

        Result<String> recoverResult = recoverVehicleAggregateRootApi.toCheck(recoverVehicleDTO);
        if (recoverResult.getCode() != 0) {
            return recoverResult.getMsg();
        }
        // 删除缓存中暂存的验车信息
        String serveNo = recoverVechicleCmd.getServeNo();
        String key = DeliverUtils.concatCacheKey(Constants.RECOVER_VEHICLE_CHECK_INFO_CACHE_KEY, serveNo, "*");
        Set<String> keys = redisTools.getKeys(key);
        if (!keys.isEmpty()) {
            redisTools.delKeys(new ArrayList<>(keys));
            log.info("收车验车信息保存操作，服务单号为{}的收车验车暂存信息被删除，key分别是:{}", serveNo, keys);
        }

        // 保存费用到计费域
        /*CreateVehicleDamageCmd cmd = new CreateVehicleDamageCmd();
        cmd.setServeNo(serve.getServeNo());
        cmd.setOrderId(serve.getOrderId());
        cmd.setCustomerId(serve.getCustomerId());
        cmd.setCarNum(deliverDTO.getCarNum());
        cmd.setFrameNum(deliverDTO.getFrameNum());
        cmd.setDamageFee(recoverVechicleCmd.getDamageFee());
        cmd.setParkFee(recoverVechicleCmd.getParkFee());
        Result<Integer> createVehicleDamageResult = vehicleDamageAggregateRootApi.createVehicleDamage(cmd);
        if (createVehicleDamageResult.getCode() != 0) {
            // 目前没有分布式事务，如果保存费用失败不应影响后续逻辑的执行
            log.error("收车时验车，保存费用到计费域失败，serveNo：{}", serve.getServeNo());
        }

        //更新交付单状态未 已验车 已收车
        Result<Integer> deliverResult = deliverAggregateRootApi.toCheck(recoverVechicleCmd.getServeNo(), recoverVechicleCmd.get);
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

        // 发送收车信息到mq，由合同域判断服务单所属的合同是否到已履约完成状态
        ServeDTO serveDTOToNoticeContract = new ServeDTO();
        serveDTOToNoticeContract.setServeNo(serve.getServeNo());
        serveDTOToNoticeContract.setOaContractCode(serve.getOaContractCode());
        serveDTOToNoticeContract.setGoodsId(serve.getGoodsId());
        serveDTOToNoticeContract.setCarServiceId(recoverVechicleCmd.getCarServiceId());
        serveDTOToNoticeContract.setRenewalType(serve.getRenewalType());
        mqTools.send(topic, "recover_serve_to_contract", null, JSON.toJSONString(serveDTOToNoticeContract));

        DeliverCarServiceDTO deliverCarServiceDTO = new DeliverCarServiceDTO();
        deliverCarServiceDTO.setCarServiceId(recoverVechicleCmd.getCarServiceId());
        deliverCarServiceDTO.setServeNoList(Arrays.asList(recoverVechicleCmd.getServeNo()));
        deliverAggregateRootApi.saveCarServiceId(deliverCarServiceDTO);

        //收车计费
        RecoverVehicleCmd recoverVehicleCmd = new RecoverVehicleCmd();
        recoverVehicleCmd.setServeNo(serveNo);
        recoverVehicleCmd.setVehicleId(deliverDTO.getCarId());
        recoverVehicleCmd.setDeliverNo(deliverDTO.getDeliverNo());
        recoverVehicleCmd.setCustomerId(serve.getCustomerId());
        recoverVehicleCmd.setCreateId(recoverVechicleCmd.getCarServiceId());
        recoverVehicleCmd.setRecoverDate(DateUtil.formatDate(recoverVechicleCmd.getRecoverVehicleTime()));
        mqTools.send(topic, "recover_vehicle", null, JSON.toJSONString(recoverVehicleCmd));
        //同步
        syncServiceI.execOne(recoverVechicleCmd.getServeNo());
        return deliverResult.getMsg();*/
        return null;
    }
}

