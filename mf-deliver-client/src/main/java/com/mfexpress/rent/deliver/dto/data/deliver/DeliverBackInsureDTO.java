package com.mfexpress.rent.deliver.dto.data.deliver;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data

public class DeliverBackInsureDTO {

    private List<String> serveNoList;

    private Integer insuranceRemark;

    private Date insuranceTime;

}



