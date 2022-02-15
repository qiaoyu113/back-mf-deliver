package com.mfexpress.rent.deliver.recovervehicle.executor;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.mfexpress.billing.rentcharge.dto.data.deliver.RecoverVehicleCmd;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.mq.MqTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.constant.ContractFailureReasonEnum;
import com.mfexpress.rent.deliver.constant.ElecHandoverContractStatus;
import com.mfexpress.rent.deliver.consumer.sync.SyncServiceImpl;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ElecHandoverContractAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.cmd.CancelContractCmd;
import com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto.ElecContractDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverAbnormalCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.dto.data.MaintenanceDTO;
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

import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j
public class RecoverAbnormalCmdExe {

    @Resource
    private ElecHandoverContractAggregateRootApi contractAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private MaintenanceAggregateRootApi maintenanceAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private WarehouseAggregateRootApi warehouseAggregateRootApi;

    @Resource
    private SyncServiceImpl syncServiceI;

    private MqTools mqTools;

    @Resource
    private BeanFactory beanFactory;

    @Value("${rocketmq.listenEventTopic}")
    private String event;

    public Integer execute(RecoverAbnormalCmd cmd, TokenInfo tokenInfo) {
        if(null == mqTools){
            mqTools = beanFactory.getBean(MqTools.class);
        }
        // 判断deliver中的合同状态，如果是已完成状态，不可进行此操作
        Result<ElecContractDTO> contractDTOResult = contractAggregateRootApi.getContractDTOByContractId(cmd.getElecContractId());
        ElecContractDTO contractDTO = ResultDataUtils.getInstance(contractDTOResult).getDataOrException();
        if (ElecHandoverContractStatus.COMPLETED.getCode() == contractDTO.getStatus()) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "电子交接单已签署完成，不可进行异常收车操作");
        }

        // 数据准备
        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(cmd.getServeNo());
        ServeDTO serveDTO = ResultDataUtils.getInstance(serveDTOResult).getDataOrException();

        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(cmd.getServeNo());
        DeliverDTO deliverDTO = ResultDataUtils.getInstance(deliverDTOResult).getDataOrException();

        // deliver 收车签署状态改为未签，并且异常收车flag改为真，状态改为已收车；服务单状态更改为已收车；补充异常收车信息；取出合同信息修改收车单；
        cmd.setOperatorId(tokenInfo.getId());
        cmd.setElecContractDTO(contractDTO);
        Result<MaintenanceDTO> maintainResult = maintenanceAggregateRootApi.getMaintenanceByServeNo(cmd.getServeNo());
        if (ResultValidUtils.checkResult(maintainResult)) {
            MaintenanceDTO maintenanceDTO = maintainResult.getData();
            //格式化为yyyy-MM-dd
            String confirmDate = DateUtil.formatDate(maintenanceDTO.getConfirmDate());
            if (cmd.getRecoverTime().before(DateUtil.parseDate(confirmDate))) {
                throw new CommonException(ResultErrorEnum.UPDATE_ERROR.getCode(), "收车日期请晚于维修交车日期");
            }
        }
        Result<Integer> operationResult = recoverAggregateRootApi.abnormalRecover(cmd);
        ResultValidUtils.checkResultException(operationResult);

        //更新车辆状态
        VehicleSaveCmd vehicleSaveCmd = new VehicleSaveCmd();
        vehicleSaveCmd.setId(Collections.singletonList(deliverDTO.getCarId()));
        vehicleSaveCmd.setSelectStatus(ValidSelectStatusEnum.UNCHECKED.getCode());
        vehicleSaveCmd.setStockStatus(ValidStockStatusEnum.IN.getCode());
        vehicleSaveCmd.setWarehouseId(contractDTO.getRecoverWareHouseId());
        vehicleSaveCmd.setCustomerId(0);
        Result<WarehouseDto> wareHouseResult = warehouseAggregateRootApi.getWarehouseById(vehicleSaveCmd.getWarehouseId());
        if (wareHouseResult.getData() != null) {
            vehicleSaveCmd.setAddress(wareHouseResult.getData().getName());
        }
        Result<String> changeVehicleStatusResult = vehicleAggregateRootApi.saveVehicleStatusById(vehicleSaveCmd);
        if (ResultErrorEnum.SUCCESSED.getCode() != changeVehicleStatusResult.getCode()) {
            log.error("异常收车时，更改车辆状态失败，serveNo：{}，车辆id：{}", cmd.getServeNo(), deliverDTO.getCarId());
        }

        // 合同置为失败状态，失败原因为异常收车导致撤销
        CancelContractCmd cancelContractCmd = new CancelContractCmd();
        cancelContractCmd.setContractId(contractDTO.getContractId());
        cancelContractCmd.setFailureReason(ContractFailureReasonEnum.ABNORMAL_RECOVER_CANCEL.getCode());
        cancelContractCmd.setOperatorId(tokenInfo.getId());
        Result<Integer> cancelResult = contractAggregateRootApi.cancelContract(cancelContractCmd);
        ResultValidUtils.checkResultException(cancelResult);

        // 发送收车信息到mq，由合同域判断服务单所属的合同是否到已履约完成状态
        ServeDTO serveDTOToNoticeContract = new ServeDTO();
        serveDTOToNoticeContract.setServeNo(serveDTO.getServeNo());
        serveDTOToNoticeContract.setOaContractCode(serveDTO.getOaContractCode());
        serveDTOToNoticeContract.setGoodsId(serveDTO.getGoodsId());
        serveDTOToNoticeContract.setCarServiceId(contractDTO.getCreatorId());
        serveDTOToNoticeContract.setRenewalType(serveDTO.getRenewalType());
        log.info("异常收车时，交付域向合同域发送的收车单信息：{}", serveDTOToNoticeContract);
        mqTools.send(event, "recover_serve_to_contract", null, JSON.toJSONString(serveDTOToNoticeContract));

        //收车计费
        RecoverVehicleCmd recoverVehicleCmd = new RecoverVehicleCmd();
        recoverVehicleCmd.setServeNo(serveDTO.getServeNo());
        recoverVehicleCmd.setVehicleId(deliverDTO.getCarId());
        recoverVehicleCmd.setDeliverNo(deliverDTO.getDeliverNo());
        recoverVehicleCmd.setCustomerId(serveDTO.getCustomerId());
        recoverVehicleCmd.setCreateId(contractDTO.getCreatorId());
        recoverVehicleCmd.setRecoverDate(DateUtil.formatDate(cmd.getRecoverTime()));
        log.info("异常收车时，交付域向计费域发送的收车单信息：{}", recoverVehicleCmd);
        mqTools.send(event, "recover_vehicle", null, JSON.toJSONString(recoverVehicleCmd));

        //同步
        Map<String, String> map = new HashMap<>();
        map.put("serve_no", cmd.getServeNo());
        syncServiceI.execOne(map);

        return 0;
    }

}
