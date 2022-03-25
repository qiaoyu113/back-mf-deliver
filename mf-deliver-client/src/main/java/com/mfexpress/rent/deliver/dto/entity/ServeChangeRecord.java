package com.mfexpress.rent.deliver.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @deprecated 此类是PO，不是entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "serve_change_record")
@Builder
public class ServeChangeRecord {

    @Id
    private Integer id;

    private String serveNo;

    private Integer type;

    private Integer renewalType;

    private Integer rawGoodsId;

    private Integer newGoodsId;

    private String newBillingAdjustmentDate;

    private String rawData;

    private String newData;

    private Integer creatorId;

    private Date createTime;

}
