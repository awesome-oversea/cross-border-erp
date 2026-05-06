package com.aidotnet.erp.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan(basePackages = {
        "com.aidotnet.erp.common.persistence.mapper",
        "com.aidotnet.erp.iam.infrastructure.mapper",
        "com.aidotnet.erp.pdm.infrastructure.mapper",
        "com.aidotnet.erp.som.infrastructure.mapper",
        "com.aidotnet.erp.ads.infrastructure.mapper",
        "com.aidotnet.erp.oms.infrastructure.mapper",
        "com.aidotnet.erp.scm.infrastructure.mapper",
        "com.aidotnet.erp.wms.infrastructure.mapper",
        "com.aidotnet.erp.fba.infrastructure.mapper",
        "com.aidotnet.erp.tms.infrastructure.mapper",
        "com.aidotnet.erp.crm.infrastructure.mapper",
        "com.aidotnet.erp.fms.infrastructure.mapper",
        "com.aidotnet.erp.bi.infrastructure.mapper",
        "com.aidotnet.erp.dashboard.infrastructure.mapper",
        "com.aidotnet.erp.sys.infrastructure.mapper"
})
@SpringBootApplication(scanBasePackages = "com.aidotnet.erp")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.aidotnet.erp")
public class ErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpApplication.class, args);
    }
}
