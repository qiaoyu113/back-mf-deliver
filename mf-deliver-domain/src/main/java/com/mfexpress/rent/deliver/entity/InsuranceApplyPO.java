package com.mfexpress.rent.deliver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "insurance_apply")
@Builder
public class InsuranceApplyPO {

    @Id
    private Integer id;

    private String deliverNo;

    private Integer type;

    private Integer vehicleId;

    private String applyTime;

    private String compulsoryBatchAcceptCode;

    private String commercialBatchAcceptCode;

    private String compulsoryApplyId;

    private String compulsoryApplyCode;

    private String commercialApplyId;

    private String commercialApplyCode;

    private String compulsoryPolicyId;

    private String compulsoryPolicyNo;

    private Integer compulsoryPolicySource;

    private String commercialPolicyId;

    private String commercialPolicyNo;

    private Integer commercialPolicySource;

    private Integer delFlag;

    private Date createTime;

    private Integer creatorId;

    private Date updateTime;

    private Integer updaterId;

}
