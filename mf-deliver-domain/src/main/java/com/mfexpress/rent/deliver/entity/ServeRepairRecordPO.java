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
@Table(name = "serve_repair_record")
@Builder
public class ServeRepairRecordPO {

    @Id
    private Integer id;

    private String serveNo;

    private Long maintenanceId;

    private Integer delFlag;

    private Date createTime;

}
