package com.aidotnet.erp.common.storage;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.tenant.TenantContext;
import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/sys/api/in/v1/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    private final MinioStorageService storageService;

    public FileController(MinioStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "module", defaultValue = "general") String module) throws Exception {
        String tenantId = TenantContext.getTenantId();
        String objectName = storageService.uploadTenantFile(module, file.getOriginalFilename(),
                file.getInputStream(), file.getSize(), file.getContentType());
        String presignedUrl = storageService.getPresignedUrl(objectName, Duration.ofHours(1));
        log.info("File uploaded: tenant={}, module={}, object={}", tenantId, module, objectName);
        return Result.ok(Map.of(
                "objectName", objectName,
                "url", presignedUrl,
                "module", module,
                "originalName", file.getOriginalFilename() != null ? file.getOriginalFilename() : ""
        ));
    }

    @GetMapping("/download/{objectName}")
    public ResponseEntity<InputStream> download(@PathVariable String objectName) throws Exception {
        InputStream stream = storageService.download(objectName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + objectName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(stream);
    }

    @GetMapping("/presigned-url/{objectName}")
    public Result<Map<String, String>> getPresignedUrl(
            @PathVariable String objectName,
            @RequestParam(value = "expiryMinutes", defaultValue = "60") int expiryMinutes) throws Exception {
        String url = storageService.getPresignedUrl(objectName, Duration.ofMinutes(expiryMinutes));
        return Result.ok(Map.of("url", url, "objectName", objectName));
    }

    @DeleteMapping("/{objectName}")
    public Result<Void> delete(@PathVariable String objectName) throws Exception {
        storageService.delete(objectName);
        log.info("File deleted: object={}", objectName);
        return Result.ok(null);
    }
}
