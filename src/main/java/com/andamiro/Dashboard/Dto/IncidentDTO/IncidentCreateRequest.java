package com.andamiro.Dashboard.Dto.IncidentDTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record IncidentCreateRequest(
        UUID userId,
        String title,
        String description,
        String incidentType,
        String location,
        LocalDateTime happenedAt

) {}
