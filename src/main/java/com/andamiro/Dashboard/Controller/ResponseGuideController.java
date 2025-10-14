package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.ResponseGuideDTO.*;
import com.andamiro.Dashboard.Security.CustomPrincipal;
import com.andamiro.Dashboard.Service.ResponseGuideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/incidents/{id}/response-guide")
@RequiredArgsConstructor
@Tag(name = "Response Guide API", description = "맞춤형 대응 가이드 생성 및 조회 API")
public class ResponseGuideController {

    private final ResponseGuideService responseGuideService;

    /* 04-01: 맞춤형 대응 가이드 전략 생성 */
    @Operation(summary = "맞춤형 대응 가이드 전략 생성", description = "사고 유형에 맞는 대응 가이드를 생성합니다.")
    @PostMapping
    public ResponseEntity<ResponseGuideCreateResponse> createGuide(@AuthenticationPrincipal CustomPrincipal principal, @PathVariable UUID id) {
        ResponseGuideCreateResponse response = responseGuideService.createGuide(principal.getId(), id);
        return ResponseEntity.status(201).body(response);
    }

    /* 04-02: 맞춤형 대응 가이드 조회 */
    @Operation(summary = "맞춤형 대응 가이드 조회", description = "사고에 대한 대응 가이드 리스트를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<ResponseGuideResponse>> getGuides(@AuthenticationPrincipal CustomPrincipal principal, @PathVariable UUID id) {
        List<ResponseGuideResponse> response = responseGuideService.getGuides(principal.getId(), id);
        return ResponseEntity.ok(response);
    }
}
