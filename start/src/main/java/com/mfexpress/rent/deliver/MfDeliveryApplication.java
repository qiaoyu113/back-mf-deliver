package com.mfexpress.rent.deliver;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = {"com.mfexpress.rent.deliver", "com.mfexpress.rent.vehicle.fallback", "com.mfexpress.order.fallback","com.mfexpress.billing.rentcharge.fallback"})
@EnableFeignClients(basePackages = {"com.mfexpress.rent.deliver.domainapi",
        "com.mfexpress.rent.deliver.api", "com.mfexpress.rent.vehicle.api",
        "com.mfexpress.order.api.app","com.mfexpress.common.domain.api","com.mfexpress.transportation.customer.api","com.mfexpress.billing.rentcharge.api"})
@EnableDiscoveryClient
@MapperScan(basePackages = {"com.mfexpress.rent.deliver.deliver.repository",
        "com.mfexpress.rent.deliver.delivervehicle.repository", "com.mfexpress.rent.deliver.serve.repository", "com.mfexpress.rent.deliver.recovervehicle.repository"})
public class MfDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MfDeliveryApplication.class, args);
    }
}
