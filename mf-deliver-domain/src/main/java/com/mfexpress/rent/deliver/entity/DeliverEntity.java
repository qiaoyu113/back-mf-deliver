package com.mfexpress.rent.deliver.entity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import com.mfexpress.rent.deliver.constant.DeliverStatusEnum;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCmd;
import com.mfexpress.rent.deliver.entity.api.DeliverEntityApi;
import com.mfexpress.rent.deliver.gateway.DeliverGateway;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "deliver")
@Builder
@Component
public class DeliverEntity implements DeliverEntityApi {

    @Resource
    private DeliverGateway deliverGateway;

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
}
