package com.mfexpress.rent.deliver;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {"com.mfexpress.rent.deliver", "com.mfexpress.rent.vehicle.fallback", "com.mfexpress.order.fallback", "com.mfexpress.billing.rentcharge.fallback"
        , "com.mfexpress.transportation.customer.fallback", "com.mfexpress.common.domain.api.fallback", "com.mfexpress.billing.customer.fallback"})
@EnableFeignClients(basePackages = {"com.mfexpress.rent.deliver.domainapi",
        "com.mfexpress.rent.deliver.api", "com.mfexpress.rent.deliver.externalApi", "com.mfexpress.rent.vehicle.api",
        "com.mfexpress.order.api.app", "com.mfexpress.common.domain.api", "com.mfexpress.transportation.customer.api", "com.mfexpress.billing.rentcharge.api",
        "com.mfexpress.component.starter.feign.contract", "com.mfexpress.component.starter.feign.excel", "com.mfexpress.rent.maintain.api.app",
        "com.mfexpress.billing.customer.api.aggregate", "com.hx.backmarket.insurance.domainapi"})
@EnableDiscoveryClient
@MapperScan(basePackages = {"com.mfexpress.rent.deliver.*.repository"})
@EnableScheduling
public class MfDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MfDeliveryApplication.class, args);
    }
}
