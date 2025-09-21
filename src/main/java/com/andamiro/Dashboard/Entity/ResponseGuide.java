package com.andamiro.Dashboard.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "response_guides")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false)
    private Incident.IncidentType incidentType;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String checklist; // JSON 문자열로 저장 (DB가 PostgreSQL JSON 타입이면 변경 가능)

    @Column(name = "legal_clause", columnDefinition = "TEXT")
    private String legalClause;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public static ResponseGuide create(Incident.IncidentType type, String title,
                                       String desc, String checklist, String clause) {
        ResponseGuide g = new ResponseGuide();
        g.incidentType = type;
        g.title = title;
        g.description = desc;
        g.checklist = checklist;
        g.legalClause = clause;
        return g;
    }
}

