package com.aidotnet.erp.common.storage;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(
            @Value("${erp.minio.endpoint:http://localhost:9000}") String endpoint,
            @Value("${erp.minio.access-key:changeme}") String accessKey,
            @Value("${erp.minio.secret-key:changeme}") String secretKey) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
