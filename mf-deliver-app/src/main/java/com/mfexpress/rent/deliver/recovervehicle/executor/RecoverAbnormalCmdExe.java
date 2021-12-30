package com.mfexpress.rent.deliver.recovervehicle.executor;

import cn.hutool.core.date.DateUtil;
import com.mfexpress.billing.rentcharge.api.DailyAggregateRootApi;
import com.mfexpress.billing.rentcharge.api.VehicleDamageAggregateRootApi;
import com.mfexpress.billing.rentcharge.dto.data.VehicleDamage.CreateVehicleDamageCmd;
import com.mfexpress.billing.rentcharge.dto.data.daily.DailyDTO;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.component.utils.util.ResultValidUtils;
import com.mfexpress.rent.deliver.constant.ContractFailureReasonEnum;
import com.mfexpress.rent.deliver.constant.DeliverContractStatusEnum;
import com.mfexpress.rent.deliver.constant.ElecHandoverContractStatus;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
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
import com.mfexpress.rent.deliver.elecHandoverContract.executor.cmd.CancelContractCmdExe;
import com.mfexpress.rent.vehicle.api.VehicleAggregateRootApi;
import com.mfexpress.rent.vehicle.api.WarehouseAggregateRootApi;
import com.mfexpress.rent.vehicle.constant.ValidSelectStatusEnum;
import com.mfexpress.rent.vehicle.constant.ValidStockStatusEnum;
import com.mfexpress.rent.vehicle.data.dto.vehicle.VehicleSaveCmd;
import com.mfexpress.rent.vehicle.data.dto.warehouse.WarehouseDto;
import lombok.extern.slf4j.Slf4j;
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
    private VehicleDamageAggregateRootApi vehicleDamageAggregateRootApi;

    @Resource
    private DailyAggregateRootApi dailyAggregateRootApi;

    @Resource
    private VehicleAggregateRootApi vehicleAggregateRootApi;

    @Resource
    private WarehouseAggregateRootApi warehouseAggregateRootApi;

    @Resource
    private SyncServiceImpl syncServiceI;

    public Integer execute(RecoverAbnormalCmd cmd, TokenInfo tokenInfo) {
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
        Result<Integer> operationResult = recoverAggregateRootApi.abnormalRecover(cmd);
        ResultValidUtils.checkResultException(operationResult);

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
            log.error("异常收车时，更改车辆状态失败，serveNo：{}，车辆id：{}", cmd.getServeNo(), deliverDTO.getCarId());
        }

        // 合同置为失败状态，失败原因为异常收车导致撤销
        CancelContractCmd cancelContractCmd = new CancelContractCmd();
        cancelContractCmd.setContractId(contractDTO.getContractId());
        cancelContractCmd.setFailureReason(ContractFailureReasonEnum.ABNORMAL_RECOVER_CANCEL.getCode());
        cancelContractCmd.setOperatorId(tokenInfo.getId());
        Result<Integer> cancelResult = contractAggregateRootApi.cancelContract(cancelContractCmd);
        ResultValidUtils.checkResultException(cancelResult);

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
            log.error("异常收车时，保存费用到计费域失败，serveNo：{}", cmd.getServeNo());
        }

        // 缺少收车日报mq逻辑

        //同步
        Map<String, String> map = new HashMap<>();
        map.put("serve_no", cmd.getServeNo());
        syncServiceI.execOne(map);

        return 0;
    }

}
