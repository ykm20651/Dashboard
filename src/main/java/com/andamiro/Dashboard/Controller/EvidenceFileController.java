package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.EvidenceFileDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Evidence File API", description = "증거자료 등록, 수정, 삭제, 조회 API")
public class EvidenceFileController {

    /* 02-01: 증거자료 목록 조회 */
    @Operation(summary = "증거자료 목록 조회", description = "해당 사고의 증거자료 리스트를 조회합니다.")
    @GetMapping("/incidends/{id}/evidence-files")
    public ResponseEntity<List<EvidenceFileResponse>> getEvidenceFiles(@PathVariable UUID id) {
        EvidenceFileResponse response = new EvidenceFileResponse(
                UUID.randomUUID(),
                "https://cdn.service.com/evidence1.jpg",
                "image",
                "사고 현장 사진",
                UUID.randomUUID(),
                LocalDateTime.now()
        );
        return ResponseEntity.ok(List.of(response));
    }

    /* 02-02: 증거자료 업로드 */
    @Operation(summary = "증거자료 업로드", description = "사고에 대한 증거자료(이미지/영상)를 업로드합니다.")
    @PostMapping("/incidents/{id}/evidence-files")
    public ResponseEntity<EvidenceFileCreateResponse> uploadEvidenceFile(
            @PathVariable UUID id,
            @RequestBody EvidenceFileCreateRequest request
    ) {
        EvidenceFileCreateResponse response = new EvidenceFileCreateResponse(
                UUID.randomUUID(),
                id,
                request.fileUrl(),
                request.fileType(),
                request.description(),
                UUID.randomUUID(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(201).body(response);
    }

    /* 02-03: 증거자료 삭제 */
    @Operation(summary = "증거자료 삭제", description = "증거자료를 삭제합니다.")
    @DeleteMapping("/evidence-files/{id}")
    public ResponseEntity<Void> deleteEvidenceFile(@PathVariable UUID id) {
        return ResponseEntity.noContent().build();
    }

    /* 02-04: 증거자료 수정 */
    @Operation(summary = "증거자료 수정", description = "증거자료 설명을 수정합니다.")
    @PutMapping("/evidence-files/{id}")
    public ResponseEntity<EvidenceFileUpdateResponse> updateEvidenceFile(@PathVariable UUID id, @RequestBody EvidenceFileUpdateRequest request) {
        EvidenceFileUpdateResponse response = new EvidenceFileUpdateResponse(
                id,
                UUID.randomUUID(), // incidentId 더미 값
                "https://cdn.service.com/evidence2.mp4",
                "video",
                request.description(),
                LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }
}
