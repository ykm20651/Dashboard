package com.andamiro.Dashboard.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "evidence_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvidenceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false,  columnDefinition = "CHAR(36)")
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploader_id", columnDefinition = "CHAR(36)", nullable = false)
    private User uploader;

    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum FileType { IMAGE, VIDEO }

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public static EvidenceFile create(Incident incident, User uploader, String url, FileType type) {
        EvidenceFile e = new EvidenceFile();
        e.incident = incident;
        e.uploader = uploader;
        e.fileUrl = url;
        e.fileType = type;
        return e;
    }

    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void updateDescription(String newDescription) {
        this.description = newDescription;
    }

}
