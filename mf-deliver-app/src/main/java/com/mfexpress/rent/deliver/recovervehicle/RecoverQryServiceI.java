package com.mfexpress.rent.deliver.recovervehicle;

import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverQryListCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverTaskListVO;

public interface RecoverQryServiceI {

    RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd);
}
