package com.mfexpress.rent.deliver.dto.data.serve;

import lombok.Data;
import java.util.Date;

@Data
public class ServeChangeRecordDTO {

    private Integer id;

    private String serveNo;

    private Integer renewalType;

    private Integer rawGoodsId;

    private Integer newGoodsId;

    private String rawData;

    private String newData;

    private Integer creatorId;

    private Date createTime;

}
