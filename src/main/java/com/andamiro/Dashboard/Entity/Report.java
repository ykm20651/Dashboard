package com.andamiro.Dashboard.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false, columnDefinition = "CHAR(36)")
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "generated_by", nullable = false, columnDefinition = "CHAR(36)")
    private User generatedBy;


    @Column(name = "pdf_url", nullable = false, columnDefinition = "TEXT")
    private String pdfUrl;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    public static Report create(Incident incident, User generatedBy, String pdfUrl) {
        Report r = new Report();
        r.incident = incident;
        r.generatedBy = generatedBy;
        r.pdfUrl = pdfUrl;
        r.generatedAt = LocalDateTime.now();
        return r;
    }
}

