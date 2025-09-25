package com.andamiro.Dashboard.Dto.IncidentDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "사고 응답 DTO")
public record IncidentResponse(
        UUID id,
        String title,
        String incidentType,
        String location,
        String description,
        LocalDateTime happenedAt,
        LocalDateTime reportedAt,
        String status,
        CreatorSummary creator //작성자 요약
) {
    public record CreatorSummary(
            UUID id, // 작성자(User의 id) = 사고를 등록한 사용자의 id
            String name
    ){}

}

/*
클라이언트 요청 JSON 키 이름과 DTO 필드 명이 같아야 한다!!
결국 이 JSON 키 이름은 프론트에서 fetch같은 걸로 HTTP 요청 보낼 때 JSON 키 이름에서 결정됨.
이걸 API 명세서에서 http 메서드, url매핑, 요청/응답 JSON 양식을 개발할 각각의 api 비즈니스 로직에다가 명세하는 것이다!

* */