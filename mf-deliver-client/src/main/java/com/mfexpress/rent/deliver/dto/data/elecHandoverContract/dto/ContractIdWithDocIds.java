package com.mfexpress.rent.deliver.dto.data.elecHandoverContract.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ContractIdWithDocIds {

    private Long ContractId;

    private Map<String, String> deliverNoWithDocId;

}
