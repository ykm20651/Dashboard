package com.andamiro.Dashboard.Controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    /*
    표시(썸네일/미리보기) = /files/evidence/{filename} (inline, 캐시, 헤더 불필요)
    다운로드(권한/감사) = /evidence-files/{id} (attachment, 인증·권한 검사)

     */
    /* 02-05 API 증거자료 다운로드 - 썸네일표시 매핑 */
    @GetMapping("/evidence/{fileName:.+}")
    public ResponseEntity<?> getEvidenceFile(@PathVariable String fileName) {
        try {
            String decodedName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
            Path filePath = Paths.get(System.getProperty("user.dir") + "/uploads/evidence/").resolve(decodedName);

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(404).body("{\"error\": \"파일을 찾을 수 없습니다.\"}");
            }

            Resource resource = new FileSystemResource(filePath.toFile());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + decodedName + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"파일 읽기 실패\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }



}
