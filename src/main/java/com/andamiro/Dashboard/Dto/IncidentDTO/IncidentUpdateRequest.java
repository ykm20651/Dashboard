package com.andamiro.Dashboard.Dto.IncidentDTO;

import com.andamiro.Dashboard.Entity.Incident;

import java.time.LocalDateTime;

public record IncidentUpdateRequest(
        String title,
        String description,
        String location,
        String incidentType,
        LocalDateTime happenedAt,
        String status
) {
}
