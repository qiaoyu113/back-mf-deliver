package com.mfexpress.rent.deliver.recovervehicle;

import com.mfexpress.component.dto.TokenInfo;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverQryListCmd;
import com.mfexpress.rent.deliver.dto.data.recovervehicle.RecoverTaskListVO;

public interface RecoverQryServiceI {

    RecoverTaskListVO execute(RecoverQryListCmd recoverQryListCmd, TokenInfo tokenInfo);
}
