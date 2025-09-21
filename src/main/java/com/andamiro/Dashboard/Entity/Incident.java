package com.andamiro.Dashboard.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "incidents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", columnDefinition = "CHAR(36)", nullable = false)
    private User creator;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false)
    private IncidentType incidentType;

    private String location;

    @Column(name = "happened_at", nullable = false)
    private LocalDateTime happenedAt;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Incident (사고 상세 화면에서 증거 목록도 조회할 수 있게끔)
    @OneToMany(mappedBy = "incident")
    private List<EvidenceFile> evidenceFiles = new ArrayList<>();

    // Incident (사고 상세 화면에서 보고서 생성도 할 수 있게끔)
    @OneToMany(mappedBy = "incident")
    private List<Report> reports = new ArrayList<>();

    // incident_response_guide_map (중간 테이블) 사용
    @ManyToMany
    @JoinTable(
            name = "incident_response_guide_map",
            joinColumns = @JoinColumn(name = "incident_id", columnDefinition = "CHAR(36)"),
            inverseJoinColumns = @JoinColumn(name = "guide_id", columnDefinition = "CHAR(36)")
    )
    private Set<ResponseGuide> responseGuides = new HashSet<>();

    public enum IncidentType { OIL_SPILL, COLLISION, FIRE, ETC }
    public enum Status { OPEN, REPORT_GENERATED, CLOSED }

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public static Incident create(User creator, String title, String desc,
                                  IncidentType type, String location, LocalDateTime happenedAt) {
        Incident i = new Incident();
        i.creator = creator;
        i.title = title;
        i.description = desc;
        i.incidentType = type;
        i.location = location;
        i.happenedAt = happenedAt;
        i.reportedAt = LocalDateTime.now();
        return i;
    }

    public void close() { this.status = Status.CLOSED; }
    public void markReportGenerated() { this.status = Status.REPORT_GENERATED; }
}

