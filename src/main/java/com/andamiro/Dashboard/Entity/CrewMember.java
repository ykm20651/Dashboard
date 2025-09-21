package com.andamiro.Dashboard.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "crew_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrewMember {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name ="id", columnDefinition = "CHAR(36)")
    private User user;

    // 소속된 선주 (여러 명의 crew가 한 owner에게 속할 수 있으므로 ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_owner", columnDefinition = "CHAR(36)", nullable = false)
    private Owner assignedOwner;

    @Column(nullable = false, length = 50)
    private String position; // 직책 (예: 기관사, 선장 등)

    // 승인한 관리자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by",columnDefinition = "CHAR(36)")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    // ──────────────── 팩토리 메서드 ──────────────── 빌더 패턴 x 엔티티에는
    public static CrewMember create(User user, Owner assignedOwner, String position) {
        CrewMember crew = new CrewMember();
        crew.user = user;
        crew.assignedOwner = assignedOwner;
        crew.position = position;
        return crew;
    }

    // ──────────────── 도메인 메서드 ────────────────
    public void approve(User approver) {
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }


}
