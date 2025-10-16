package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Client.FastApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FastApiClient fastApiClient;

    /*보고서 다운로드 API */
    @GetMapping("/report/{taskId}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable String taskId) {
        byte[] pdf = fastApiClient.downloadReport(taskId);

        if (pdf == null || pdf.length == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + taskId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
