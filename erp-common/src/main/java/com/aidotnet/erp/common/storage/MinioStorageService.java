package com.aidotnet.erp.common.storage;

import com.aidotnet.erp.common.tenant.TenantContext;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MinioStorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageService.class);
    private final MinioClient minioClient;

    @Value("${erp.minio.default-bucket:erp-files}")
    private String defaultBucket;

    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void ensureBucket(String bucket) throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            log.info("Created MinIO bucket: {}", bucket);
        }
    }

    public String upload(String objectName, InputStream stream, long size, String contentType) throws Exception {
        ensureBucket(defaultBucket);
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(defaultBucket)
                .object(objectName)
                .stream(stream, size, -1)
                .contentType(contentType)
                .build());
        log.debug("Uploaded file: bucket={}, object={}", defaultBucket, objectName);
        return objectName;
    }

    public String uploadTenantFile(String module, String fileName, InputStream stream, long size, String contentType) throws Exception {
        String tenantId = TenantContext.getTenantId();
        String objectName = buildTenantPath(tenantId, module, fileName);
        return upload(objectName, stream, size, contentType);
    }

    public InputStream download(String objectName) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(defaultBucket)
                .object(objectName)
                .build());
    }

    public void delete(String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(defaultBucket)
                .object(objectName)
                .build());
        log.debug("Deleted file: bucket={}, object={}", defaultBucket, objectName);
    }

    public String getPresignedUrl(String objectName, Duration expiry) throws Exception {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(defaultBucket)
                .object(objectName)
                .method(Method.GET)
                .expiry((int) expiry.toSeconds())
                .build());
    }

    public String getPresignedUploadUrl(String objectName, Duration expiry) throws Exception {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(defaultBucket)
                .object(objectName)
                .method(Method.PUT)
                .expiry((int) expiry.toSeconds())
                .build());
    }

    public String buildTenantPath(String tenantId, String module, String fileName) {
        String ext = "";
        int dotIdx = fileName.lastIndexOf('.');
        if (dotIdx > 0) {
            ext = fileName.substring(dotIdx);
        }
        String uniqueName = UUID.randomUUID().toString().replace("-", "") + ext;
        return tenantId + "/" + module + "/" + uniqueName;
    }
}
