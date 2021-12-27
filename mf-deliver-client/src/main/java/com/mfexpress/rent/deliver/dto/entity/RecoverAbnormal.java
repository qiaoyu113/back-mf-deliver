package com.mfexpress.rent.deliver.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "recover_abnormal")
public class RecoverAbnormal {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    private String serveNo;

    private String deliverNo;

    private Integer reporterId;

    private String reporterName;

    private String reporterPhone;

    private String cause;

    private String imgUrl;

    private Date createTime;

    private Integer creatorId;

}
