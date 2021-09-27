package com.mfexpress.rent.deliver.dto.data.deliver;

import lombok.Data;

import java.util.List;

@Data
public class DeliverCarServiceDTO {
    private List<String> serveNoList;

    private Integer carServiceId;
}
