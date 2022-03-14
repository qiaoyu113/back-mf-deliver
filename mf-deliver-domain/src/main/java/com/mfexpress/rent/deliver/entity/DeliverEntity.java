package com.mfexpress.rent.deliver.entity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import com.mfexpress.rent.deliver.constant.DeliverStatusEnum;
import com.mfexpress.rent.deliver.dto.data.deliver.DeliverDTO;
import com.mfexpress.rent.deliver.dto.data.serve.ReactivateServeCmd;
import com.mfexpress.rent.deliver.entity.api.DeliverEntityApi;
import com.mfexpress.rent.deliver.gateway.DeliverGateway;
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
        List<DeliverEntity> deliverList = deliverGateway.getDeliverByServeNoList(serveNoList);
        if (CollectionUtil.isEmpty(deliverList)) {
            return null;
        }
        return BeanUtil.copyToList(deliverList, DeliverDTO.class, new CopyOptions().ignoreError());
    }

    public void toHistory(ReactivateServeCmd cmd) {
        DeliverEntity deliverEntity = new DeliverEntity();
        deliverEntity.setStatus(DeliverStatusEnum.HISTORICAL.getCode());
        deliverGateway.updateDeliverByDeliverNo(cmd.getDeliverNo(), deliverEntity);
    }
}
