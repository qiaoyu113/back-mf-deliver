package com.mfexpress.rent.deliver.recovervehicle.executor;

import com.alibaba.fastjson.JSON;
import com.mfexpress.billing.customer.api.aggregate.AdvincePaymentAggregateRootApi;
import com.mfexpress.billing.rentcharge.dto.data.deliver.DeductFeeCmd;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.response.ResultStatusEnum;
import com.mfexpress.component.starter.tools.mq.MqTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.api.ServeServiceI;
import com.mfexpress.rent.deliver.constant.ReplaceVehicleDepositPayTypeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverDeductionByDeliverCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import com.mfexpress.rent.deliver.dto.data.serve.dto.ServeAdjustRecordDTO;
import com.mfexpress.rent.deliver.dto.data.serve.qry.ServeAdjustRecordQry;
import com.mfexpress.rent.deliver.utils.MainServeUtil;
import com.mfexpress.rent.maintain.api.app.MaintenanceAggregateRootApi;
import com.mfexpress.rent.maintain.dto.data.ReplaceVehicleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class RecoverDeductionByDeliverExe {

    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private MaintenanceAggregateRootApi maintenanceAggregateRootApi;

    @Resource
    private AdvincePaymentAggregateRootApi advincePaymentAggregateRootApi;

    @Resource
    private ServeServiceI serveServiceI;

    @Resource
    private MqTools mqTools;

    @Value("${rocketmq.listenEventTopic}")
    private String topic;

    public Integer execute(RecoverDeductionByDeliverCmd cmd, TokenInfo tokenInfo) {
        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByDeliverNo(cmd.getDeliverNo());
        if (ResultErrorEnum.SUCCESSED.getCode() != deliverDTOResult.getCode() || deliverDTOResult.getData() == null) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "查询交付单失败");
        }
        DeliverDTO deliverDTO = deliverDTOResult.getData();

        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(deliverDTO.getServeNo());
        if (ResultErrorEnum.SUCCESSED.getCode() != serveDTOResult.getCode() || serveDTOResult.getData() == null) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "查询服务单失败");
        }
        ServeDTO serveDTO = serveDTOResult.getData();

        DeliverDTO deliverDTOToUpdate = new DeliverDTO();
        BeanUtils.copyProperties(cmd, deliverDTOToUpdate);
        deliverDTOToUpdate.setUpdateId(tokenInfo.getId());
        Result<Integer> result = deliverAggregateRootApi.toDeductionByDeliver(deliverDTOToUpdate);
        if (result.getCode() != 0) {
            throw new CommonException(result.getCode(), result.getMsg());
        }

        //生成消分代办金额扣罚项
        DeductFeeCmd deductFeeCmd = new DeductFeeCmd();
        deductFeeCmd.setDamage(BigDecimal.valueOf(cmd.getDamageFee() == null ? 0 : cmd.getDamageFee()));
        deductFeeCmd.setPark(BigDecimal.valueOf(cmd.getParkFee() == null ? 0 : cmd.getParkFee()));
        deductFeeCmd.setCreateId(tokenInfo.getId());
        deductFeeCmd.setServeNo(serveDTO.getServeNo());
        deductFeeCmd.setVehicleId(deliverDTO.getCarId());
        deductFeeCmd.setDeliverNo(deliverDTO.getDeliverNo());
        deductFeeCmd.setCustomerId(serveDTO.getCustomerId());
        if (cmd.getDeductionHandel().equals(3)) {
            deductFeeCmd.setAgency(cmd.getAgencyAmount());
            deductFeeCmd.setEliminate(cmd.getDeductionAmount());
        }

        mqTools.send(topic, "deduct_fee", null, JSON.toJSONString(deductFeeCmd));
        DeliverCarServiceDTO deliverCarServiceDTO = new DeliverCarServiceDTO();
        deliverCarServiceDTO.setServeNoList(Collections.singletonList(serveDTO.getServeNo()));
        deliverCarServiceDTO.setCarServiceId(tokenInfo.getId());
        deliverAggregateRootApi.saveCarServiceId(deliverCarServiceDTO);

        Result<Integer> updateResult = recoverVehicleAggregateRootApi.updateDeductionFeeByDeliver(cmd);
        if (ResultStatusEnum.SUCCESSED.getCode() != updateResult.getCode() || null == updateResult.getData()) {
            log.error("在收车进行处理事项操作时，修改收车单失败");
        }

        // 查询是否有替换车服务单
        ReplaceVehicleDTO replaceVehicleDTO = MainServeUtil.getReplaceVehicleDTOBySourceServNo(maintenanceAggregateRootApi, serveDTO.getServeNo());
        if (Optional.ofNullable(replaceVehicleDTO).isPresent()) {
            // 查询押金支付方式
            Result<ServeAdjustRecordDTO> recordDTOResult = serveAggregateRootApi.getServeAdjustRecord(new ServeAdjustRecordQry(replaceVehicleDTO.getServeNo()));
            ServeAdjustRecordDTO serveAdjustRecordDTO = ResultDataUtils.getInstance(recordDTOResult).getDataOrException();
            if (Optional.ofNullable(serveAdjustRecordDTO).filter(o -> ReplaceVehicleDepositPayTypeEnum.SOURCE_DEPOSIT_PAY.getCode() == o.getDepositPayType()).isPresent()) {
                // 账本扣除
                Result<ServeDTO> replaceServeDTOResult = serveAggregateRootApi.getServeDtoByServeNo(replaceVehicleDTO.getServeNo());
                ServeDTO replaceServe = ResultDataUtils.getInstance(replaceServeDTOResult).getDataOrException();

                // 原车服务单押金解除锁定
                List<String> serveNoList = new ArrayList<>();
                serveNoList.add(serveDTO.getServeNo());
                serveAggregateRootApi.unLockDeposit(serveNoList, tokenInfo.getId());

                serveServiceI.serveDepositPay(replaceServe, tokenInfo.getId());
            }
        }

        return result.getData();
    }
}
