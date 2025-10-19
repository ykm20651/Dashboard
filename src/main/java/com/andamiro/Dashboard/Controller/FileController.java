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

    private static final String UPLOAD_DIR_REPORT = "/app/uploads/reports/";
    private static final String UPLOAD_DIR_EVIDENCE = "/app/uploads/evidence/";

    /* 03-04 보고서 파일 반환 (inline 미리보기 or 다운로드) */
    @GetMapping("/reports/{fileName:.+}")
    public ResponseEntity<?> downloadReport(@PathVariable String fileName) {
        return serveLocalFile(UPLOAD_DIR_REPORT, fileName, true);
    }

    /* 02-05 증거자료 썸네일/미리보기 반환 (inline) */
    @GetMapping("/evidence/{fileName:.+}")
    public ResponseEntity<?> getEvidenceFile(@PathVariable String fileName) {
        return serveLocalFile(UPLOAD_DIR_EVIDENCE, fileName, true);
    }


    /* 공통 파일 반환 로직 */
    private ResponseEntity<?> serveLocalFile(String baseDir, String fileName, boolean inline) {
        try {
            // URL 인코딩되어 있을 수 있는 파일명 디코딩
            String decodedName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
            Path filePath = Paths.get(baseDir).resolve(decodedName);

            if (!Files.exists(filePath)) {
                return ResponseEntity.status(404)
                        .body("{\"error\": \"파일을 찾을 수 없습니다.\"}");
            }

            Resource resource = new FileSystemResource(filePath.toFile());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            // inline: 브라우저에서 바로 보여주기 / attachment: 다운로드 유도
            String disposition = inline ? "inline" : "attachment";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + decodedName + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"파일 읽기 실패\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
