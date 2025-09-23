package com.andamiro.Dashboard.Dto.IncidentDTO;

import java.time.LocalDateTime;

public record IncidentCreateRequest(
        String title,
        String description,
        String incidentType,
        String location,
        LocalDateTime happenedAt

) {}
