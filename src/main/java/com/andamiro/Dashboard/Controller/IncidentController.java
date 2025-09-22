package com.andamiro.Dashboard.Controller;


import com.andamiro.Dashboard.Dto.IncidentDTO.*;
import com.andamiro.Dashboard.Service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/incidents")
@RequiredArgsConstructor
@Tag(name ="Incident API", description = "사고 등록, 관리, 조회 API")
public class IncidentController {

    private final IncidentService incidentService; //@RequiredArgsConstructor

    /* 01-01 API 건 매핑 */
    @Operation(summary ="사고 목록 조회", description = "선원/선주 본인이 등록한 사고의 리스트 조회")
    @GetMapping
    public ResponseEntity<List<IncidentResponse>> getIncident(@RequestParam(required = true) UUID id){
        List<IncidentResponse> response = incidentService.getListIncidents(id);
        return ResponseEntity.ok(response);
    }

    /* 01-02 API 건 매핑 */
    @Operation(summary = "사고 등록", description = "선원/선주가 새로운 사고 정보를 등록합니다.")
    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(@RequestBody IncidentCreateRequest request) {
        return ResponseEntity.status(201).body(incidentService.createIncident(request));
    }

    /* 01-03: 사고 상세 조회 */
    @Operation(summary = "사고 상세 조회", description = "특정 사고의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<IncidentDetailResponse> getDetailIncident(@PathVariable UUID id) {
        IncidentDetailResponse response = incidentService.getDetailIncident(id);
        return ResponseEntity.ok(response);
    }

    /* 01-04: 사고 수정 */
    @Operation(summary = "사고 수정", description = "사고 정보를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<IncidentUpdateResponse> updateIncident(@PathVariable UUID id, @RequestBody IncidentUpdateRequest request) {
        // TODO: service.updateIncident(id, request)
        IncidentUpdateResponse response = incidentService.updateIncident(id, request);
        return ResponseEntity.ok(response);
    }

    /* 01-05: 사고 삭제 */
    @Operation(summary = "사고 삭제", description = "사고를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable UUID id) {
        // TODO: service.deleteIncident(id)
        incidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }


}
