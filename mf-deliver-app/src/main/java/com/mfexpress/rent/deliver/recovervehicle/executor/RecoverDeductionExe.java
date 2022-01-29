package com.mfexpress.rent.deliver.recovervehicle.executor;


import cn.hutool.core.date.DateUtil;
import com.mfexpress.billing.rentcharge.api.DeductAggrgateRootApi;
import com.mfexpress.billing.rentcharge.constant.BusinessChargeTypeEnum;
import com.mfexpress.billing.rentcharge.dto.data.deduct.DeductDTO;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.mq.relation.binlog.EsSyncHandlerI;
import com.mfexpress.rent.deliver.constant.JudgeEnum;
import com.mfexpress.rent.deliver.domainapi.DeliverAggregateRootApi;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverCarServiceDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverDeductionCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ServeDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

@Component
public class RecoverDeductionExe {


    @Resource
    private DeliverAggregateRootApi deliverAggregateRootApi;

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private DeductAggrgateRootApi deductAggrgateRootApi;
    @Resource
    private EsSyncHandlerI syncServiceI;


    public String execute(RecoverDeductionCmd recoverDeductionCmd) {
        DeliverDTO deliverDTO = new DeliverDTO();

        BeanUtils.copyProperties(recoverDeductionCmd, deliverDTO);
        Result<String> result = deliverAggregateRootApi.toDeduction(deliverDTO);

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
            Result<ServeDTO> serveDTOResult = serveAggregateRootApi.getServeDtoByServeNo(recoverDeductionCmd.getServeNo());
            Result<DeliverDTO> deliverDTOResult = deliverAggregateRootApi.getDeliverByServeNo(recoverDeductionCmd.getServeNo());
            if (serveDTOResult.getData() != null && deliverDTOResult.getData() != null) {
                ServeDTO serveDTO = serveDTOResult.getData();
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
        }
        DeliverCarServiceDTO deliverCarServiceDTO = new DeliverCarServiceDTO();
        deliverCarServiceDTO.setServeNoList(Arrays.asList(recoverDeductionCmd.getServeNo()));
        deliverCarServiceDTO.setCarServiceId(recoverDeductionCmd.getCarServiceId());
        deliverAggregateRootApi.saveCarServiceId(deliverCarServiceDTO);

        Map<String, String> map = new HashMap<>();
        map.put("serve_no", recoverDeductionCmd.getServeNo());
        syncServiceI.execOne(map);

        return serveResult.getData();
    }
}
