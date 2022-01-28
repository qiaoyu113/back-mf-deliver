package com.mfexpress.rent.deliver.recovervehicle.executor;


import cn.hutool.core.date.DateUtil;
import com.mfexpress.billing.rentcharge.api.DeductAggrgateRootApi;
import com.mfexpress.billing.rentcharge.api.VehicleDamageAggregateRootApi;
import com.mfexpress.billing.rentcharge.constant.BusinessChargeTypeEnum;
import com.mfexpress.billing.rentcharge.dto.data.VehicleDamage.CreateVehicleDamageCmd;
import com.mfexpress.billing.rentcharge.dto.data.deduct.DeductDTO;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.response.ResultStatusEnum;
import com.mfexpress.rent.deliver.api.SyncServiceI;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.RecoverVehicleAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverDeductionCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Component
@Slf4j
public class RecoverDeductionExe {


    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;

    @Resource
    private DeductAggrgateRootApi deductAggrgateRootApi;

    @Resource
    private RecoverVehicleAggregateRootApi recoverVehicleAggregateRootApi;

    @Resource
    private VehicleDamageAggregateRootApi vehicleDamageAggregateRootApi;

    @Resource
    private SyncServiceI syncServiceI;


    public String execute(RecoverDeductionCmd recoverDeductionCmd) {
        Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(recoverDeductionCmd.getServeNo());
        if (ResultErrorEnum.SUCCESSED.getCode() != serveDTOResult.getCode() || serveDTOResult.getData() == null) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "查询服务单失败");
        }
        ServeDTO serveDTO = serveDTOResult.getData();

        Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(recoverDeductionCmd.getServeNo());
        if (ResultErrorEnum.SUCCESSED.getCode() != deliverDTOResult.getCode() || deliverDTOResult.getData() == null) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "查询交付单失败");
        }
        DeliverDTO deliverDTO = deliverDTOResult.getData();

        DeliverDTO deliverDTOToUpdate = new DeliverDTO();
        BeanUtils.copyProperties(recoverDeductionCmd, deliverDTOToUpdate);
        Result<String> result = deliverAggregateRootApi.toDeduction(deliverDTOToUpdate);
        if (result.getCode() != 0) {
            throw new CommonException(result.getCode(), result.getMsg());

        }
        Result<String> serveResult = serveAggregateRootApi.completed(recoverDeductionCmd.getServeNo());
        if (serveResult.getCode() != 0) {
            throw new CommonException(serveResult.getCode(), serveResult.getMsg());
        }
        //生成消分代办金额扣罚项
        List<DeductDTO> deductDTOList = new LinkedList<>();
        if (recoverDeductionCmd.getDeductionHandel().equals(3)) {
            DeliverDTO deliverDTO1 = deliverDTOResult.getData();
            if (recoverDeductionCmd.getDeductionAmount().compareTo(BigDecimal.ZERO) != 0) {
                DeductDTO deductDTO = new DeductDTO();
                deductDTO.setServeNo(recoverDeductionCmd.getServeNo());
                deductDTO.setCustomerId(serveDTO.getCustomerId());
                deductDTO.setOrderId(serveDTO.getOrderId());
                deductDTO.setStatus(JudgeEnum.NO.getCode());
                deductDTO.setDeductPoints(recoverDeductionCmd.getViolationPoints());
                deductDTO.setCreateDate(DateUtil.formatDate(new Date()));
                deductDTO.setCarNum(deliverDTO1.getCarNum());
                deductDTO.setFrameNum(deliverDTO1.getFrameNum());
                deductDTO.setType(BusinessChargeTypeEnum.DEDUCT_ELIMINATE.getCode());
                deductDTO.setAmount(recoverDeductionCmd.getDeductionAmount());
                deductDTOList.add(deductDTO);
            }
            if (recoverDeductionCmd.getAgencyAmount().compareTo(BigDecimal.ZERO) != 0) {
                DeductDTO deductDTO = new DeductDTO();
                deductDTO.setServeNo(recoverDeductionCmd.getServeNo());
                deductDTO.setCustomerId(serveDTO.getCustomerId());
                deductDTO.setOrderId(serveDTO.getOrderId());
                deductDTO.setStatus(JudgeEnum.NO.getCode());
                deductDTO.setDeductPoints(recoverDeductionCmd.getViolationPoints());
                deductDTO.setCreateDate(DateUtil.formatDate(new Date()));
                deductDTO.setCarNum(deliverDTO1.getCarNum());
                deductDTO.setFrameNum(deliverDTO1.getFrameNum());
                deductDTO.setType(BusinessChargeTypeEnum.DEDUCT_AGENCY.getCode());
                deductDTO.setAmount(recoverDeductionCmd.getAgencyAmount());
                deductDTOList.add(deductDTO);
            }
            deductAggrgateRootApi.createDeduct(deductDTOList);
        }
        DeliverCarServiceDTO deliverCarServiceDTO = new DeliverCarServiceDTO();
        deliverCarServiceDTO.setServeNoList(Arrays.asList(recoverDeductionCmd.getServeNo()));
        deliverCarServiceDTO.setCarServiceId(recoverDeductionCmd.getCarServiceId());
        deliverAggregateRootApi.saveCarServiceId(deliverCarServiceDTO);

        // 保存车损费和停车费到收车单中
        Result<Integer> updateResult = recoverVehicleAggregateRootApi.updateDeductionFee(recoverDeductionCmd);
        if(ResultStatusEnum.SUCCESSED.getCode() != updateResult.getCode() || null == updateResult.getData()){
            log.error("在收车进行处理事项操作时，修改收车单失败");
        }

        // 保存费用到计费域
        CreateVehicleDamageCmd cmd = new CreateVehicleDamageCmd();
        cmd.setServeNo(serveDTO.getServeNo());
        cmd.setOrderId(serveDTO.getOrderId());
        cmd.setCustomerId(serveDTO.getCustomerId());
        cmd.setCarNum(deliverDTO.getCarNum());
        cmd.setFrameNum(deliverDTO.getFrameNum());
        cmd.setDamageFee(recoverDeductionCmd.getDamageFee());
        cmd.setParkFee(recoverDeductionCmd.getParkFee());
        Result<Integer> createVehicleDamageResult = vehicleDamageAggregateRootApi.createVehicleDamage(cmd);
        if (createVehicleDamageResult.getCode() != 0) {
            // 目前没有分布式事务，如果保存费用失败不应影响后续逻辑的执行
            log.error("收车时验车，保存费用到计费域失败，serveNo：{}", serveDTO.getServeNo());
        }

        syncServiceI.execOne(recoverDeductionCmd.getServeNo());

        return serveResult.getData();
    }
}
