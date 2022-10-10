package com.mfexpress.rent.deliver.externalApi;

import com.mfexpress.component.dto.proxy.OutsideRequestDTO;
import com.mfexpress.component.response.Result;
import com.mfexpress.rent.deliver.externalApi.fallback.BackMarketApiFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mf-base-outside", path = "/api/outside/v1/proxy", contextId = "mf-outside-backMarket"/*, fallback = BackMarketApiFallback.class*/)
public interface BackMarketApi {

    @PostMapping("/postRequest")
    String postRequest(@RequestBody @Validated OutsideRequestDTO outsideRequestDTO);
}
