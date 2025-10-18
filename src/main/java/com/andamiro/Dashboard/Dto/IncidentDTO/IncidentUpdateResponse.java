package com.andamiro.Dashboard.Dto.IncidentDTO;

import com.andamiro.Dashboard.Entity.Incident;

import java.time.LocalDateTime;
import java.util.UUID;

public record IncidentUpdateResponse(
        UUID id,
        String title,
        String description,
        String incidentType,
        String location,
        LocalDateTime happenedAt,
        LocalDateTime updatedAt,
        String status
        //Incident.IncidentType incidentType으로 하지 않은 이유는, Enum은 어차피 Json 직렬화 할 때 문자열로 감싸져서 나감.
        //Enum은 전부 String으로 받고 Service단에서 Incident.IncidentType.valueOf(request.incidentType()) enum화하기/
) {
}
