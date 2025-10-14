package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.IncidentDTO.*;
import com.andamiro.Dashboard.Security.CustomPrincipal;
import com.andamiro.Dashboard.Service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/incidents")
@RequiredArgsConstructor
@Tag(name ="Incident API", description = "사고 등록, 관리, 조회 API")
public class IncidentController {

    private final IncidentService incidentService;

    /* 01-01 API 사고 목록 조회 매핑 */
    @Operation(summary ="사고 목록 조회", description = "선원/선주 본인이 소속된 사고의 리스트 조회")
    @GetMapping
    public ResponseEntity<List<IncidentResponse>> getIncidents(
            @AuthenticationPrincipal CustomPrincipal principal) {
        return ResponseEntity.ok(incidentService.getListIncidents(principal.getId()));
    }

    /* 01-02 API 사고 등록 매핑 */
    @Operation(summary = "사고 등록", description = "선원/선주가 새로운 사고 정보를 등록합니다.")
    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(
            @AuthenticationPrincipal CustomPrincipal principal,
            @RequestBody IncidentCreateRequest request) {
        try {
            return ResponseEntity.status(201).body(incidentService.createIncident(principal.getId(), request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /* 01-03 API 사고 상세 조회 매핑 */
    @Operation(summary = "사고 상세 조회", description = "특정 사고의 상세 정보를 조회합니다. (Owner 전용, 본인 소유만)")
    @GetMapping("/{id}")
    public ResponseEntity<IncidentDetailResponse> getDetailIncident(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(incidentService.getDetailIncident(principal.getId(), id));
    }

    /* 01-04 API 사고 수정 매핑 */
    @Operation(summary = "사고 수정", description = "사고 정보를 수정합니다. (Owner 전용, 본인 소유만)")
    @PutMapping("/{id}")
    public ResponseEntity<IncidentUpdateResponse> updateIncident(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable UUID id,
            @RequestBody IncidentUpdateRequest request) {
        return ResponseEntity.ok(incidentService.updateIncident(principal.getId(), id, request));
    }

    /* 01-05 API 사고 삭제 매핑 */
    @Operation(summary = "사고 삭제", description = "사고를 삭제합니다. (Owner 전용, 본인 소유만)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable UUID id) {
        try {
            incidentService.deleteIncident(principal.getId(), id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
