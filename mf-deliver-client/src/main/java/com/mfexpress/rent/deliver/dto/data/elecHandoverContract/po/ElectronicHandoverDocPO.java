package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "electronic_handover_doc")
public class ElectronicHandoverDocPO {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id")
    private Integer id;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "deliver_no")
    private String deliverNo;

    @Column(name = "deliver_type")
    private Integer deliverType;

    @Column(name = "deliver_or_recover_no")
    private String deliverOrRecoverNo;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "valid_status")
    private Integer validStatus;

}
