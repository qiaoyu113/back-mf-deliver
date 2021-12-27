package com.mfexpress.rent.deliver.dto.data.recovervehicle;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("异常收车信息DTO")
public class RecoverAbnormalDTO {

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
