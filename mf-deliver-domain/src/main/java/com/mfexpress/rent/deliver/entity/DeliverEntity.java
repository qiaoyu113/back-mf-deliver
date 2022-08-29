package com.mfexpress.rent.deliver.entity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.exception.CommonException;
import com.mfexpress.rent.deliver.constant.*;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverInsureCmd;
import com.mfexpress.rent.deliver.dto.data.deliver.cmd.*;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.DeliverBatchInsureApplyDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.DeliverInsureApplyDTO;
import com.mfexpress.rent.deliver.dto.data.deliver.dto.InsuranceApplyDTO;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverBackInsureByDeliverCmd;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCmd;
import com.mfexpress.rent.deliver.entity.api.DeliverEntityApi;
import com.mfexpress.rent.deliver.gateway.DeliverGateway;
import com.mfexpress.rent.deliver.gateway.InsuranceApplyGateway;
import com.mfexpress.rent.deliver.utils.DeliverUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "deliver")
@Builder
@Component
public class DeliverEntity implements DeliverEntityApi {

    @Resource
    private DeliverGateway deliverGateway;

    @Resource
    private InsuranceApplyGateway insuranceApplyGateway;

    @Id
    private Integer id;

    private String deliverNo;

    private Long deliverId;

    private Integer customerId;

    private String serveNo;

    private Integer isCheck;

    private Integer isInsurance;

    private Integer isDeduction;

    private Integer insuranceRemark;

    private Integer carId;

    private Double mileage;

    private Double vehicleAge;

    private String carNum;

    private String frameNum;

    private Integer deliverStatus;

    private Integer status;

    private Integer saleId;

    private Integer carServiceId;

    private Integer createId;

    private Integer updateId;

    private Date createTime;

    private Date updateTime;

    private Integer deductionHandel;

    private Integer violationPoints;

    private BigDecimal deductionAmount;

    private BigDecimal agencyAmount;

    private String insuranceStartTime;

    private String insuranceEndTime;

    private Integer deliverContractStatus;

    private Integer recoverContractStatus;

    private Integer recoverAbnormalFlag;

    @Override
    public List<DeliverDTO> getDeliverDTOListByServeNoList(List<String> serveNoList) {
        if (CollectionUtil.isEmpty(serveNoList)) {
            return CollectionUtil.newArrayList();
        }
        List<DeliverEntity> deliverList = deliverGateway.getDeliverByServeNoList(serveNoList);
        return CollectionUtil.isEmpty(deliverList) ? CollectionUtil.newArrayList() : BeanUtil.copyToList(deliverList, DeliverDTO.class, new CopyOptions().ignoreError());
    }

    @Override
    public DeliverDTO getDeliverDTOByCarId(Integer carId) {
        List<DeliverEntity> deliverEntityList = deliverGateway.getDeliverByCarId(carId);
        if (CollectionUtil.isEmpty(deliverEntityList)) {
            return null;
        }
        DeliverDTO deliverDTO = new DeliverDTO();
        BeanUtil.copyProperties(deliverEntityList.get(0), deliverDTO, new CopyOptions().ignoreError());
        return deliverDTO;
    }


    @Override
    public List<DeliverDTO> getDeliverNotComplete(List<String> serveNoList) {
        if (CollectionUtil.isEmpty(serveNoList)) {
            return CollectionUtil.newArrayList();
        }
        List<DeliverEntity> deliverList = deliverGateway.getDeliverNotCompleteByServeNoList(serveNoList);

        return CollectionUtil.isEmpty(deliverList) ? CollectionUtil.newArrayList() : BeanUtil.copyToList(deliverList, DeliverDTO.class, new CopyOptions().ignoreError());
    }

    @Override
    public DeliverDTO getDeliverByDeliverNo(String deliverNo) {
        DeliverEntity deliverEntity = deliverGateway.getDeliverByDeliverNo(deliverNo);
        if (Objects.isNull(deliverEntity)) {
            return null;
        }
        DeliverDTO deliverDTO = new DeliverDTO();
        BeanUtil.copyProperties(deliverEntity, deliverDTO, new CopyOptions().ignoreError());
        return deliverDTO;
    }

    @Override
    public void toHistory(ReactivateServeCmd cmd) {
        DeliverEntity deliverEntity = new DeliverEntity();
        deliverEntity.setStatus(DeliverStatusEnum.HISTORICAL.getCode());
        deliverGateway.updateDeliverByDeliverNo(cmd.getDeliverNo(), deliverEntity);
    }

    @Override
    @Transactional
    public void cancelDeliver(DeliverCancelCmd cmd) {

        DeliverEntity deliverEntity = new DeliverEntity();
        deliverEntity.setDeliverStatus(DeliverEnum.CANCEL.getCode());
        deliverEntity.setStatus(DeliverStatusEnum.INVALID.getCode());
        deliverEntity.setUpdateId(cmd.getOperatorId());
        deliverEntity.setUpdateTime(new Date());

        deliverGateway.updateDeliverByServeNo(cmd.getServeNo(), deliverEntity);
    }

    @Override
    @Transactional
    public void completedDeliver(DeliverCompletedCmd cmd) {

        DeliverEntity deliver = DeliverEntity.builder().deliverStatus(DeliverEnum.DELIVER.getCode())
                .updateId(cmd.getOperatorId()).build();

        deliverGateway.updateDeliverByDeliverNo(cmd.getDeliverNo(), deliver);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insureByCompany(DeliverInsureCmd cmd) {
        // 修改交付单的发车投保申请状态
        DeliverBatchInsureApplyDTO deliverBatchInsureApplyDTO = cmd.getDeliverBatchInsureApplyDTO();
        List<DeliverInsureApplyDTO> deliverInsureApplyDTOS = deliverBatchInsureApplyDTO.getDeliverInsureApplyDTOS();
        List<String> deliverNos = deliverInsureApplyDTOS.stream().map(DeliverInsureApplyDTO::getDeliverNo).collect(Collectors.toList());
        DeliverEntity deliverEntity = DeliverEntity.builder().updateId(cmd.getOperatorId()).build();
        deliverGateway.updateDeliverByDeliverNos(deliverNos, deliverEntity);
        // 插入投保申请数据

        List<InsuranceApplyPO> insuranceApplyPOS = deliverInsureApplyDTOS.stream().map(deliverInsureApplyDTO -> {
            InsuranceApplyPO insuranceApplyPO = new InsuranceApplyPO();
            insuranceApplyPO.setDeliverNo(deliverInsureApplyDTO.getDeliverNo());
            insuranceApplyPO.setType(InsuranceApplyTypeEnum.INSURE.getCode());
            /*if (!StringUtils.isEmpty(deliverInsureApplyDTO.getCompulsoryInsurancePolicyNo())) {
                insuranceApplyPO.setCompulsoryPolicyNo(deliverInsureApplyDTO.getCompulsoryInsurancePolicyNo());
                insuranceApplyPO.setCompulsoryPolicySource(PolicySourceEnum.EXISTS.getCode());
            } else {
                insuranceApplyPO.setCompulsoryBatchAcceptCode(deliverBatchInsureApplyDTO.getCompulsoryBatchAcceptCode());
                insuranceApplyPO.setCompulsoryApplyId(deliverInsureApplyDTO.getCompulsoryApplyId());
                insuranceApplyPO.setCompulsoryApplyCode(deliverInsureApplyDTO.getCompulsoryApplyCode());
            }

            if (!StringUtils.isEmpty(deliverInsureApplyDTO.getCommercialInsurancePolicyNo())) {
                insuranceApplyPO.setCommercialPolicyNo(deliverInsureApplyDTO.getCommercialInsurancePolicyNo());
                insuranceApplyPO.setCommercialPolicySource(PolicySourceEnum.EXISTS.getCode());
            } else {
                insuranceApplyPO.setCommercialBatchAcceptCode(deliverBatchInsureApplyDTO.getCommercialBatchAcceptCode());
                insuranceApplyPO.setCommercialApplyId(deliverInsureApplyDTO.getCommercialApplyId());
                insuranceApplyPO.setCommercialApplyCode(deliverInsureApplyDTO.getCommercialApplyCode());
            }*/

            insuranceApplyPO.setCompulsoryBatchAcceptCode(deliverBatchInsureApplyDTO.getCompulsoryBatchAcceptCode());
            insuranceApplyPO.setCompulsoryApplyId(deliverInsureApplyDTO.getCompulsoryApplyId());
            insuranceApplyPO.setCompulsoryApplyCode(deliverInsureApplyDTO.getCompulsoryApplyCode());

            insuranceApplyPO.setCommercialBatchAcceptCode(deliverBatchInsureApplyDTO.getCommercialBatchAcceptCode());
            insuranceApplyPO.setCommercialApplyId(deliverInsureApplyDTO.getCommercialApplyId());
            insuranceApplyPO.setCommercialApplyCode(deliverInsureApplyDTO.getCommercialApplyCode());

            insuranceApplyPO.setCreatorId(cmd.getOperatorId());
            insuranceApplyPO.setApplyTime(DateUtil.formatDateTime(deliverInsureApplyDTO.getApplyTime()));
            return insuranceApplyPO;
        }).collect(Collectors.toList());
        insuranceApplyGateway.batchCreate(insuranceApplyPOS);

        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insureByCustomer(DeliverInsureByCustomerCmd cmd) {
        /*if (!StringUtils.isEmpty(applyPO.getCommercialPolicyId())) {
            throw new CommonException(ResultErrorEnum.OPER_ERROR.getCode(), "商业险保单已存在");
        }*/

        DeliverEntity deliverEntityToUpdate = new DeliverEntity();
        deliverEntityToUpdate.setDeliverNo(cmd.getDeliverNo());
        deliverEntityToUpdate.setIsInsurance(JudgeEnum.YES.getCode());
        deliverEntityToUpdate.setUpdateId(cmd.getOperatorId());
        deliverGateway.updateDeliverByDeliverNo(cmd.getDeliverNo(), deliverEntityToUpdate);

        InsuranceApplyPO originalApplyPO = insuranceApplyGateway.getByDeliverNoAndType(cmd.getDeliverNo(), InsuranceApplyTypeEnum.INSURE.getCode());
        InsuranceApplyPO applyPOToUpdate = new InsuranceApplyPO();
        // 补充保单号
        applyPOToUpdate.setCommercialPolicyId(cmd.getCommercialPolicyId());
        applyPOToUpdate.setCommercialPolicyNo(cmd.getPolicyNo());
        applyPOToUpdate.setCommercialPolicySource(PolicySourceEnum.CUSTOMER.getCode());
        applyPOToUpdate.setUpdaterId(cmd.getOperatorId());
        if (null != originalApplyPO) {
            applyPOToUpdate.setId(originalApplyPO.getId());
            insuranceApplyGateway.update(applyPOToUpdate);
        } else {
            applyPOToUpdate.setDeliverNo(cmd.getDeliverNo());
            applyPOToUpdate.setType(InsuranceApplyTypeEnum.INSURE.getCode());
            insuranceApplyGateway.create(applyPOToUpdate);
        }

        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insureComplete(InsureCompleteCmd cmd) {
        DeliverEntity deliverEntity = deliverGateway.getDeliverByDeliverNo(cmd.getDeliverNo());
        if (null == deliverEntity) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "交付单查询失败");
        }

        DeliverEntity deliverEntityToUpdate = new DeliverEntity();
        deliverEntityToUpdate.setIsInsurance(JudgeEnum.YES.getCode());
        deliverGateway.updateDeliverByDeliverNo(deliverEntity.getDeliverNo(), deliverEntityToUpdate);

        InsuranceApplyPO applyPO = insuranceApplyGateway.getByDeliverNoAndType(cmd.getDeliverNo(), InsuranceApplyTypeEnum.INSURE.getCode());
        InsuranceApplyPO insuranceApplyPOToUpdate = new InsuranceApplyPO();
        if (null == applyPO) {
            insuranceApplyPOToUpdate.setDeliverNo(cmd.getDeliverNo());
            insuranceApplyPOToUpdate.setType(InsuranceApplyTypeEnum.INSURE.getCode());

            insuranceApplyPOToUpdate.setCompulsoryPolicyId(cmd.getCompulsoryInsurancePolicyId());
            insuranceApplyPOToUpdate.setCompulsoryPolicyNo(cmd.getCompulsoryInsurancePolicyNo());
            insuranceApplyPOToUpdate.setCompulsoryPolicySource(PolicySourceEnum.BACK_MARKET.getCode());
            insuranceApplyPOToUpdate.setCommercialPolicyId(cmd.getCommercialInsurancePolicyId());
            insuranceApplyPOToUpdate.setCommercialPolicyNo(cmd.getCommercialInsurancePolicyNo());
            insuranceApplyPOToUpdate.setCommercialPolicySource(PolicySourceEnum.BACK_MARKET.getCode());
            insuranceApplyGateway.create(insuranceApplyPOToUpdate);
        } else {
            insuranceApplyPOToUpdate.setCompulsoryPolicyId(cmd.getCompulsoryInsurancePolicyId());
            insuranceApplyPOToUpdate.setCompulsoryPolicyNo(cmd.getCompulsoryInsurancePolicyNo());
            insuranceApplyPOToUpdate.setCommercialPolicyId(cmd.getCommercialInsurancePolicyId());
            insuranceApplyPOToUpdate.setCommercialPolicyNo(cmd.getCommercialInsurancePolicyNo());
            if (!StringUtils.isEmpty(applyPO.getCompulsoryApplyId())) {
                insuranceApplyPOToUpdate.setCompulsoryPolicySource(PolicySourceEnum.H5.getCode());
            }
            if (!StringUtils.isEmpty(applyPO.getCommercialApplyId())) {
                insuranceApplyPOToUpdate.setCommercialPolicySource(PolicySourceEnum.H5.getCode());
            }
            insuranceApplyPOToUpdate.setId(applyPO.getId());
            insuranceApplyGateway.update(insuranceApplyPOToUpdate);
        }

        return 0;
    }

    @Override
    public List<InsuranceApplyDTO> getInsuranceApplyListByDeliverNoList(List<String> deliverNoList) {
        List<InsuranceApplyPO> insuranceApplyPOS = insuranceApplyGateway.getByDeliverNos(deliverNoList);
        return BeanUtil.copyToList(insuranceApplyPOS, InsuranceApplyDTO.class, new CopyOptions().ignoreError());
    }

    @Override
    public Integer cancelSelectedByDeliver(CancelPreSelectedCmd cmd) {
        DeliverEntity deliverEntity = deliverGateway.getDeliverByDeliverNo(cmd.getDeliverNo());
        if (null == deliverEntity) {
            throw new CommonException(ResultErrorEnum.DATA_NOT_FOUND.getCode(), "交付单查询失败");
        }
        DeliverEntity deliverEntityToUpdate = DeliverEntity.builder().status(ValidStatusEnum.INVALID.getCode()).build();
        deliverGateway.updateDeliverByDeliverNo(deliverEntity.getDeliverNo(), deliverEntityToUpdate);


        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer backInsure(RecoverBackInsureByDeliverCmd cmd) {
        DeliverEntity deliver = DeliverEntity.builder().isInsurance(JudgeEnum.YES.getCode())
                .insuranceRemark(cmd.getInsuranceRemark())
                .insuranceEndTime(DeliverUtils.dateToStringYyyyMMddHHmmss(cmd.getInsuranceTime()))
                .build();

        deliverGateway.updateDeliverByDeliverNos(cmd.getDeliverNoList(), deliver);

        List<InsuranceApplyPO> insuranceApplyPOS = cmd.getDeliverDTOList().stream().map(deliverDTO -> {
            InsuranceApplyPO insuranceApplyPO = new InsuranceApplyPO();
            insuranceApplyPO.setDeliverNo(deliverDTO.getDeliverNo());
            insuranceApplyPO.setType(InsuranceApplyTypeEnum.SURRENDER.getCode());
            insuranceApplyPO.setCommercialApplyId(deliverDTO.getSurrenderApplyId());
            insuranceApplyPO.setCommercialApplyCode(deliverDTO.getSurrenderApplyCode());
            insuranceApplyPO.setCreatorId(cmd.getOperatorId());
            insuranceApplyPO.setApplyTime(DateUtil.formatDateTime(deliverDTO.getSurrenderApplyTime()));
            return insuranceApplyPO;
        }).collect(Collectors.toList());
        insuranceApplyGateway.batchCreate(insuranceApplyPOS);
        return null;
    }

    @Override
    public InsuranceApplyDTO getInsuranceApply(InsureApplyQry qry) {
        InsuranceApplyPO applyPO = insuranceApplyGateway.getByDeliverNoAndType(qry.getDeliverNo(), qry.getType());
        if (null == applyPO) {
            return null;
        }

        InsuranceApplyDTO insuranceApplyDTO = BeanUtil.toBean(applyPO, InsuranceApplyDTO.class);
        insuranceApplyDTO.setApplyTime(DateUtil.parseDateTime(applyPO.getApplyTime()));
        return insuranceApplyDTO;
    }

    @Override
    public List<DeliverDTO> getDeliverDTOListByDeliverNoList(List<String> deliverNoList) {
        List<DeliverEntity> deliverEntityList = deliverGateway.getDeliverByDeliverNoList(deliverNoList);
        if (null == deliverEntityList || deliverEntityList.isEmpty()) {
            return null;
        }
        return BeanUtil.copyToList(deliverEntityList, DeliverDTO.class, new CopyOptions().ignoreError());
    }

    @Override
    public Integer preSelectedSupplyInsurance(List<DeliverDTO> deliverDTOList) {
        List<InsuranceApplyPO> applyPOS = new ArrayList<>();
        deliverDTOList.forEach(deliverDTO -> {
            if (!StringUtils.isEmpty(deliverDTO.getCompulsoryPolicyId()) || !StringUtils.isEmpty(deliverDTO.getCommercialPolicyId())) {
                InsuranceApplyPO applyPO = new InsuranceApplyPO();
                applyPO.setDeliverNo(deliverDTO.getDeliverNo());
                applyPO.setType(InsuranceApplyTypeEnum.INSURE.getCode());
                if (!StringUtils.isEmpty(deliverDTO.getCompulsoryPolicyId())) {
                    applyPO.setCompulsoryPolicyId(deliverDTO.getCompulsoryPolicyId());
                    applyPO.setCompulsoryPolicySource(PolicySourceEnum.EXISTS.getCode());
                }
                if (!StringUtils.isEmpty(deliverDTO.getCommercialPolicyId())) {
                    applyPO.setCommercialPolicyId(deliverDTO.getCommercialPolicyId());
                    applyPO.setCommercialPolicySource(PolicySourceEnum.EXISTS.getCode());
                }
                applyPOS.add(applyPO);
            }
        });
        insuranceApplyGateway.batchCreate(applyPOS);
        return 0;
    }

}
