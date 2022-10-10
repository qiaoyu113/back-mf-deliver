package com.mfexpress.rent.deliver.dto.data.serve;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
public class ServeReplaceVehicleDTO {

    private Integer id;

    private Long sourceServeId;
    private String sourceServeNo;

    private Long targetServeId;
    private String targetServeNo;

    /**
     * 1生效中、2已取消
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人ID
     */
    private Integer createId;
}
