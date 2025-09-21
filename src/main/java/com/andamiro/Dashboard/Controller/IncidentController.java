package com.andamiro.Dashboard.Controller;


import com.andamiro.Dashboard.Dto.IncidentDTO.IncidentResponse;
import com.andamiro.Dashboard.Entity.Incident;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/incidents")
@Tag(name ="Incident API", description = "사고 등록, 관리, 조회 API")
public class IncidentController {

    /* 01-01 API 건 매핑 */
    @Operation(summary ="사고 목록 조회", description = "선원/선주 본인이 등록한 사고의 리스트 조회")
    @GetMapping
    public ResponseEntity<IncidentResponse> getIncident(@RequestBody IncidentResponse incidentResponse) {
        return ResponseEntity.ok(incidentResponse);
    }

    /* 01-02 API 건 매핑 */




}
