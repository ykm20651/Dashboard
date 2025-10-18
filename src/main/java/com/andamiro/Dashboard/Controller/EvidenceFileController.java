package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.EvidenceFileDTO.*;
import com.andamiro.Dashboard.Security.CustomPrincipal;
import com.andamiro.Dashboard.Service.EvidenceFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Evidence File API", description = "증거자료 등록, 수정, 삭제, 조회 API")
public class EvidenceFileController {

    private final EvidenceFileService evidenceFileService;

    /* 02-01 API 해당 사고의 증거자료 목록 조회 매핑 */
    @Operation(summary = "증거자료 목록 조회", description = "해당 사고의 증거자료 리스트를 조회합니다.")
    @GetMapping("/incidents/{id}/evidence-files")
    public ResponseEntity<List<EvidenceFileResponse>> getEvidenceFiles(@PathVariable UUID id) {
        return ResponseEntity.ok(evidenceFileService.getEvidenceFiles(id));
    }

    /* 02-02 API 증거자료 추가 업로드 매핑 */
    @Operation(summary = "증거자료 업로드", description = "사고에 대한 증거자료(이미지/영상)를 업로드합니다.")
    @PostMapping(value = "/incidents/{id}/evidence-files", consumes = {"multipart/form-data"})
    public ResponseEntity<EvidenceFileCreateResponse> uploadEvidenceFile(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestPart("description") String description
    ) {
        return ResponseEntity.status(201).body(
                evidenceFileService.uploadEvidenceFile(id, principal.getId(), file, description)
        );
    }

    /* 02-03 API 증거자료 삭제 매핑 */
    @Operation(summary = "증거자료 삭제", description = "증거자료를 삭제합니다.")
    @DeleteMapping("/evidence-files/{id}")
    public ResponseEntity<Void> deleteEvidenceFile(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable UUID id) {
        evidenceFileService.deleteEvidenceFile(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    /* 02-04 API 증거자료 수정 매핑 */
    @Operation(summary = "증거자료 수정", description = "증거자료 설명을 수정합니다.")
    @PutMapping("/evidence-files/{id}")
    public ResponseEntity<EvidenceFileUpdateResponse> updateEvidenceFile(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable UUID id,
            @RequestBody EvidenceFileUpdateRequest request) {
        return ResponseEntity.ok(evidenceFileService.updateEvidenceFile(principal.getId(), id, request));
    }

    /* 02-05 API 증거자료 다운로드 매핑 */
    @Operation(summary = "증거자료 다운로드", description = "증거자료를 다운로드합니다.")
    @GetMapping("/evidence-files/{id}")
    public ResponseEntity<Resource> downloadEvidenceFile(
        @AuthenticationPrincipal CustomPrincipal principal,
        @PathVariable UUID id) {
            Resource resource = evidenceFileService.downloadEvidenceFile(principal.getId(), id);
    
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
    }
}
