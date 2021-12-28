package com.mfexpress.rent.deliver.consumer.common;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.billing.rentcharge.api.DailyAggregateRootApi;
import com.mfexpress.billing.rentcharge.api.VehicleDamageAggregateRootApi;
import com.mfexpress.billing.rentcharge.dto.data.VehicleDamage.CreateVehicleDamageCmd;
import com.mfexpress.billing.rentcharge.dto.data.daily.DailyDTO;
import com.mfexpress.billing.rentcharge.dto.data.daily.cmd.DailyOperate;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.contract.ContractResultTopicDTO;
import com.mfexpress.component.enums.contract.ContractStatusEnum;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.common.MFMqCommonProcessClass;
import com.mfexpress.component.starter.mq.relation.common.MFMqCommonProcessMethod;
import com.mfexpress.component.starter.tools.mq.MqTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.consumer.sync.SyncServiceImpl;
import com.mfexpress.rent.deliver.domainapi.*;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverContractSigningCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.ContractStatusChangeCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.DeliverImgInfo;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.WarehouseAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidStockStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.rent.vehicle.data.dto.warehouse.WarehouseDto;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.data.customer.CustomerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
@MFMqCommonProcessClass(topicKey = "rocketmq.listenContractTopic")
@Slf4j
public class ElecContractStatusMqCommand {

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private WarehouseAggregateRootApi warehouseAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private VehicleDamageAggregateRootApi vehicleDamageAggregateRootApi;

    @Resource
    private DailyAggregateRootApi dailyAggregateRootApi;

    @Resource
    private DeliverVehicleAggregateRootApi deliverVehicleAggregateRootApi;

    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;

    @Resource
    private SyncServiceImpl syncServiceI;

    @Resource
    private MqTools mqTools;

    @Value("${rocketmq.listenEventTopic}")
    private String event;

    @MFMqCommonProcessMethod(tag = Constants.THIRD_PARTY_ELEC_CONTRACT_STATUS_TAG)
    public void execute(String body) {
        log.info("mq中的合同状态信息：{}", body);
        ContractResultTopicDTO contractStatusInfo = JSONUtil.toBean(body, ContractResultTopicDTO.class);
        if(ContractStatusEnum.CREATING.getValue().equals(contractStatusInfo.getStatus())){
            // 补全三方合同编号
            ContractStatusChangeCmd cmd = new ContractStatusChangeCmd();
            cmd.setContractId(Long.valueOf(contractStatusInfo.getLocalContractId()));
            cmd.setContractForeignNo(contractStatusInfo.getThirdPartContractId());
            contractAggregateRootApi.completionContractForeignNo(cmd);
        }else if(ContractStatusEnum.SIGNING.getValue().equals(contractStatusInfo.getStatus())){
            // 合同状态改为 已创建/签署中,并补全三方合同编号；交付单中的合同状态也需改为已创建/签署中
            contractSigning(contractStatusInfo);
        }else if(ContractStatusEnum.COMPLETE.getValue().equals(contractStatusInfo.getStatus())){
            // 合同状态改为完成
            contractCompleted(contractStatusInfo);
        }else if(ContractStatusEnum.EXPIRED.getValue().equals(contractStatusInfo.getStatus())){
            // 合同状态改为失败，失败原因为过期；不需要更新交付单状态，因为失败原因为过期的话需要用户确认后才会去改变交付单的状态
            ContractStatusChangeCmd cmd = new ContractStatusChangeCmd();
            cmd.setContractForeignNo(contractStatusInfo.getThirdPartContractId());
            cmd.setFailureReason(ContractFailureReasonEnum.OVERDUE.getCode());
            contractAggregateRootApi.fail(cmd);
        }else if(ContractStatusEnum.FAIL.getValue().equals(contractStatusInfo.getStatus())){
            // 只会在合同生成中才会有此命令，合同置为失效，交付单置为未签状态
            contractFail(contractStatusInfo);
        }
    }

    private void contractFail(ContractResultTopicDTO contractStatusInfo) {
        Long contractId = Long.valueOf(contractStatusInfo.getLocalContractId());
        Result<ElecContractDTO> contractDTOResult = contractAggregateRootApi.getContractDTOByContractId(contractId);
        ElecContractDTO contractDTO = ResultDataUtils.getInstance(contractDTOResult).getDataOrException();
        if(null == contractDTO){
            log.error("收车电子合同创建完成时，根据本地合同id查询合同失败，本地合同id：{}", contractId);
            return;
        }

        ContractStatusChangeCmd cmd = new ContractStatusChangeCmd();
        cmd.setContractForeignNo(contractStatusInfo.getThirdPartContractId());
        cmd.setFailureReason(ContractFailureReasonEnum.CREATE_FAIL.getCode());
        Result<Integer> failResult = contractAggregateRootApi.fail(cmd);
        ResultValidUtils.checkResultException(failResult);

        // 当合同在创建中失败时，交付单状态不改变，需用户确认后才改变
        //deliverAggregateRootApi.makeNoSignByDeliverNo(contractDTO.getDeliverNos(), contractDTO.getDeliverType());
    }

    private void contractSigning(ContractResultTopicDTO contractStatusInfo) {
        Long contractId = Long.valueOf(contractStatusInfo.getLocalContractId());
        Result<ElecContractDTO> contractDTOResult = contractAggregateRootApi.getContractDTOByContractId(contractId);
        ElecContractDTO contractDTO = ResultDataUtils.getInstance(contractDTOResult).getDataOrException();
        if(null == contractDTO){
            log.error("收车电子合同创建完成时，根据本地合同id查询合同失败，本地合同id：{}", contractId);
            return;
        }

        ContractStatusChangeCmd cmd = new ContractStatusChangeCmd();
        cmd.setContractForeignNo(contractStatusInfo.getThirdPartContractId());
        cmd.setContractId(contractId);
        Result<Integer> contractSigningResult = contractAggregateRootApi.signing(cmd);
        ResultValidUtils.checkResultException(contractSigningResult);

        DeliverContractSigningCmd signingCmd = new DeliverContractSigningCmd();
        signingCmd.setDeliverNos(JSONUtil.toList(contractDTO.getDeliverNos(), String.class));
        signingCmd.setDeliverType(contractDTO.getDeliverType());
        deliverAggregateRootApi.contractSigning(signingCmd);
    }

    // 合同状态为已完成后触发的后续操作
    private void contractCompleted(ContractResultTopicDTO contractStatusInfo) {
        // 数据准备
        Result<ElecContractDTO> contractDTOResult = contractAggregateRootApi.getContractDTOByForeignNo(contractStatusInfo.getThirdPartContractId());
        ElecContractDTO contractDTO = ResultDataUtils.getInstance(contractDTOResult).getDataOrException();
        String deliverNo = JSONUtil.toList(contractDTO.getDeliverNos(), String.class).get(0);

        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByDeliverNo(deliverNo);
        DeliverDTO deliverDTO = ResultDataUtils.getInstance(deliverDTOResult).getDataOrException();

        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(deliverDTO.getServeNo());
        ServeDTO serveDTO = ResultDataUtils.getInstance(serveDTOResult).getDataOrException();

        ContractStatusChangeCmd cmd = new ContractStatusChangeCmd();
        cmd.setContractForeignNo(contractStatusInfo.getThirdPartContractId());
        cmd.setContractId(Long.valueOf(contractStatusInfo.getLocalContractId()));
        cmd.setDocPdfUrlMap(contractStatusInfo.getDocUrlMapping());
        contractAggregateRootApi.completed(cmd);

        List<String> serveNoList;
        if (DeliverTypeEnum.DELIVER.getCode() == contractDTO.getDeliverType()) {
            // 发车处理
            serveNoList = deliverVehicleProcess(serveDTO, contractDTO);
        } else {
            // 收车处理
            serveNoList = recoverVehicleProcess(serveDTO, deliverDTO, contractStatusInfo, contractDTO);
        }

        //同步
        Map<String, String> map = new HashMap<>();
        serveNoList.forEach(serveNo -> {
            map.put("serve_no", serveNo);
            syncServiceI.execOne(map);
        });
    }

    private List<String> recoverVehicleProcess(ServeDTO serveDTO, DeliverDTO deliverDTO, ContractResultTopicDTO contractStatusInfo, ElecContractDTO contractDTO) {
        List<String> serveNoList = new LinkedList<>();
        // 交付单、服务单修改
        Result<Integer> recoveredResult = recoverVehicleAggregateRootApi.recovered(deliverDTO.getDeliverNo(), contractStatusInfo.getThirdPartContractId());
        ResultValidUtils.checkResultException(recoveredResult);

        // 合同修改
        ContractStatusChangeCmd cmd = new ContractStatusChangeCmd();
        cmd.setContractForeignNo(contractStatusInfo.getThirdPartContractId());
        Result<Integer> signingResult = contractAggregateRootApi.completed(cmd);
        ResultValidUtils.checkResultException(signingResult);

        //更新车辆状态
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setId(Collections.singletonList(deliverDTO.getCarId()));
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.UNCHECKED.getCode());
        vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.IN.getCode());
        vehicleSaveCmd.setWarehouseId(contractDTO.getRecoverWareHouseId());
        Result<WarehouseDto> wareHouseResult = warehouseAggregateRootApi.getWarehouseById(vehicleSaveCmd.getWarehouseId());
        if (wareHouseResult.getData() != null) {
            vehicleSaveCmd.setAddress(wareHouseResult.getData().getName());
        }
        Result<String> changeVehicleStatusResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if (ResultErrorEnum.SUCCESSED.getCode() != changeVehicleStatusResult.getCode()) {
            log.error("收车电子合同签署完成时，更改车辆状态失败，serveNo：{}，车辆id：{}", serveDTO.getServeNo(), deliverDTO.getCarId());
        }

        // 保存费用到计费域
        CreateVehicleDamageCmd createVehicleDamageCmd = new CreateVehicleDamageCmd();
        createVehicleDamageCmd.setServeNo(serveDTO.getServeNo());
        createVehicleDamageCmd.setOrderId(serveDTO.getOrderId());
        createVehicleDamageCmd.setCustomerId(serveDTO.getCustomerId());
        createVehicleDamageCmd.setCarNum(deliverDTO.getCarNum());
        createVehicleDamageCmd.setFrameNum(deliverDTO.getFrameNum());
        createVehicleDamageCmd.setDamageFee(contractDTO.getRecoverDamageFee());
        createVehicleDamageCmd.setParkFee(contractDTO.getRecoverParkFee());
        Result<Integer> createVehicleDamageResult = vehicleDamageAggregateRootApi.createVehicleDamage(createVehicleDamageCmd);
        if(ResultErrorEnum.SUCCESSED.getCode() != createVehicleDamageResult.getCode()){
            // 目前没有分布式事务，如果保存费用失败不应影响后续逻辑的执行
            log.error("收车电子合同签署完成时，保存费用到计费域失败，serveNo：{}", serveDTO.getServeNo());
        }

        // 创建收车日报
        List<DailyDTO> dailyDTOList = new LinkedList<>();
        DailyDTO dailyDTO = new DailyDTO();
        dailyDTO.setCustomerId(serveDTO.getCustomerId());
        dailyDTO.setStatus(JudgeEnum.YES.getCode());
        dailyDTO.setRentDate(DateUtil.format(contractDTO.getRecoverVehicleTime(), "yyyy-MM-dd"));
        dailyDTO.setServeNo(serveDTO.getServeNo());
        dailyDTO.setDelFlag(JudgeEnum.NO.getCode());
        dailyDTOList.add(dailyDTO);
        Result<String> createDailyResult = dailyAggregateRootApi.createDaily(dailyDTOList);
        if(ResultErrorEnum.SUCCESSED.getCode() != createDailyResult.getCode()){
            log.error("收车电子合同签署完成时，保存收车日报失败，serveNo：{}", serveDTO.getServeNo());
        }

        DailyOperate operate = new DailyOperate();
        operate.setServeNo(serveDTO.getServeNo());
        operate.setCustomerId(serveDTO.getCustomerId());
        operate.setOperateDate(DateUtil.formatDate(contractDTO.getRecoverVehicleTime()));
        mqTools.send(event, "recover_vehicle", null, JSON.toJSONString(operate));

        serveNoList.add(serveDTO.getServeNo());
        return serveNoList;
    }

    private List<String> deliverVehicleProcess(ServeDTO serveDTO, ElecContractDTO contractDTO) {
        List<String> serveNoList = new LinkedList<>();
        //生成发车单 交付单状态更新已发车并初始化操作状态  服务单状态更新为已发车
        Result<Integer> result = deliverVehicleAggregateRootApi.deliverVehicles(contractDTO);
        ResultValidUtils.checkResultException(result);

        // 数据收集
        List<DailyDTO> dailyDTOList = new LinkedList<>();
        List<DeliverImgInfo> deliverImgInfos = JSONUtil.toList(contractDTO.getPlateNumberWithImgs(), DeliverImgInfo.class);
        List<Integer> carIdList = new LinkedList<>();
        deliverImgInfos.forEach(deliverImgInfo -> {
            carIdList.add(deliverImgInfo.getCarId());
            DailyDTO dailyDTO = new DailyDTO();
            dailyDTO.setServeNo(deliverImgInfo.getServeNo());
            dailyDTO.setDelFlag(JudgeEnum.NO.getCode());
            // 同一个订单下的服务单的客户都是一样的
            dailyDTO.setCustomerId(serveDTO.getCustomerId());
            dailyDTO.setRentDate(DateUtil.format(contractDTO.getDeliverVehicleTime(), "yyyy-MM-dd"));
            dailyDTO.setStatus(JudgeEnum.NO.getCode());
            dailyDTOList.add(dailyDTO);
            serveNoList.add(deliverImgInfo.getServeNo());

            //发车操作mq触发计费
            DailyOperate operate = new DailyOperate();
            operate.setServeNo(deliverImgInfo.getServeNo());
            operate.setCustomerId(serveDTO.getCustomerId());
            operate.setOperateDate(DateUtil.formatDate(contractDTO.getDeliverVehicleTime()));
            mqTools.send(event, "deliver_vehicle", null, JSON.toJSONString(operate));
        });

        // 修改对应的车辆状态为租赁状态
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.OUT.getCode());
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.LEASE.getCode());
        vehicleSaveCmd.setId(carIdList);
        vehicleSaveCmd.setCustomerId(serveDTO.getCustomerId());
        Result<CustomerVO> customerResult = customerAggregateRootApi.getById(serveDTO.getCustomerId());
        if (customerResult.getData() != null) {
            vehicleSaveCmd.setAddress(customerResult.getData().getName());
        }
        Result<String> vehicleResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if(ResultErrorEnum.SUCCESSED.getCode() != vehicleResult.getCode()){
            log.error("发车电子合同签署完成时，更改车辆状态失败。车辆id：{}", carIdList);
        }

        // 生成发车租赁日报
        dailyAggregateRootApi.createDaily(dailyDTOList);

        return serveNoList;
    }
}
