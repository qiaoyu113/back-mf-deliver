package com.mfexpress.rent.deliver.externalApi.fallback;

import com.mfexpress.component.constants.ResultErrorEnum;
import com.mfexpress.component.dto.proxy.OutsideRequestDTO;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.externalApi.BackMarketApi;
import org.springframework.stereotype.Component;

@Component
public class BackMarketApiFallback implements BackMarketApi {

    @Override
    public String postRequest(OutsideRequestDTO outsideRequestDTO) {
        return null;
    }
}
