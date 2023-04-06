package com.mfexpress.rent.deliver.dto.data;

import lombok.Data;

import java.util.List;

@Data
public class ListQry {

    private int page = 0;

    private int limit = 0;

    private List<Integer> orgIds;

    private List<Integer> customerIds;

}