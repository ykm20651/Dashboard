package com.andamiro.Dashboard.Controller;

import com.andamiro.Dashboard.Dto.ResponseGuideDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/incidents/{id}/response-guide")
@Tag(name = "Response Guide API", description = "맞춤형 대응 가이드 생성 및 조회 API")
public class ResponseGuideController {

    /* 04-01: 맞춤형 대응 가이드 전략 생성 */
    @Operation(summary = "맞춤형 대응 가이드 전략 생성", description = "사고 유형에 맞는 대응 가이드를 생성합니다.")
    @PostMapping
    public ResponseEntity<ResponseGuideCreateResponse> createGuide(@PathVariable UUID id) {
        ResponseGuideCreateResponse response = new ResponseGuideCreateResponse(
                id,
                List.of(new ResponseGuideCreateResponse.Guide(
                        UUID.randomUUID(),
                        "oil_spill",
                        "유류 유출 초기 대응 지침",
                        "즉각 해상 차단막 설치",
                        List.of("차단막 설치", "환경청 신고", "기름 회수 장비 투입"),
                        "해양환경관리법 제12조"
                )),
                LocalDateTime.now()
        );

        return ResponseEntity.status(201).body(response);
    }

    /* 04-02: 맞춤형 대응 가이드 조회 */
    @Operation(summary = "맞춤형 대응 가이드 조회", description = "사고에 대한 대응 가이드 리스트를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<ResponseGuideResponse>> getGuides(@PathVariable UUID id) {
        List<ResponseGuideResponse> response = List.of(
                new ResponseGuideResponse(
                        UUID.randomUUID(),
                        "oil_spill",
                        "유류 유출 초기 대응 지침",
                        "즉각 해상 차단막 설치",
                        List.of("차단막 설치", "환경청 신고", "기름 회수 장비 투입"),
                        "해양환경관리법 제12조",
                        LocalDateTime.now().minusDays(1)
                )
        );

        return ResponseEntity.ok(response);
    }
}
