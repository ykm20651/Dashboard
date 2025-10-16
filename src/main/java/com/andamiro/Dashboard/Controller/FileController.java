package com.andamiro.Dashboard.Controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/reports/";

    /* 03-04 API 보고서 다운로드 (로컬 파일 반환) */
    @GetMapping("/reports/{fileName:.+}")
    public ResponseEntity<?> downloadReport(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName);

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(404)
                        .body("{\"error\": \"파일을 찾을 수 없습니다.\"}");
            }

            Resource resource = new FileSystemResource(filePath.toFile());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"파일 읽기 실패\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
