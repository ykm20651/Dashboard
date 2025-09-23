package com.andamiro.Dashboard.Dto.IncidentDTO;

public record IncidentUpdateRequest(
        String title,
        String description,
        String location
) {
}
