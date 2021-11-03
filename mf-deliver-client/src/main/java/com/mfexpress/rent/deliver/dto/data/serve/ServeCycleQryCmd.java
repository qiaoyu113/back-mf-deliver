package com.mfexpress.rent.deliver.dto.data.serve;


import com.mfexpress.rent.deliver.dto.data.ListQry;
import lombok.Data;

import java.util.List;


@Data
public class ServeCycleQryCmd extends ListQry {

    private List<Integer> customerIdList;
}
