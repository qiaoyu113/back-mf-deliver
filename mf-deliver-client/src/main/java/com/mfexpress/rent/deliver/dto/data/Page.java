package com.mfexpress.rent.deliver.dto.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Page {

    private Integer total;
    private Integer nowPage;
    private Integer pages;
}
