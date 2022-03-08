package com.mfexpress.rent.deliver.dto.data.serve;

import com.mfexpress.rent.deliver.dto.data.ListQry;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("服务单押金查询")
public class ServeDepositQry extends ListQry {

    private Integer orgId;
    private Integer customerId;
    private List<Integer> statusList;


}
