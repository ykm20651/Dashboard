package com.andamiro.Dashboard.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ERD: users
 *  - id UUID [pk]
 *  - email VARCHAR [unique, not null]
 *  - password_hash TEXT
 *  - name VARCHAR
 *  - role ENUM('owner','crew') [not null]
 *  - is_approved BOOLEAN
 *  - created_at TIMESTAMP
 *  - updated_at TIMESTAMP
 */

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)// JPA 프록시/리플렉션용 기본 생성자 (외부 new 차단) - 지연로딩 - 메모리 최적화
public class User {
    
    
    @Id //PK 명시
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "CHAR(36)")
    //@GeneratedValue(strategy = GenerationType.IDENTITY) //PK값 생성 전략 정의. AUTO/IDENTITY/SEQUENCE/TABLE/UUID
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    // DB 컬럼명과 자바필드명 매핑 (snake_case ↔ camelCase)
    @Column(name = "password_hash", columnDefinition = "TEXT", nullable = false)
    private String passwordHash;

    private String name;

    @Enumerated(EnumType.STRING) // "OWNER", "CREW" 문자열로 DB에 저장
    @Column(nullable = false, length = 16)
    private Role role;

    @Column(name = "is_approved", nullable = false)
    private boolean isApproved = false; // 기본 false

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    //읽기 전용 -> 사용자가 자신이 올린 사고 리스트를 조회해보고 싶을 수 있음.
    @OneToMany(mappedBy = "creator") //Incident 엔티티 안에 있는 private User creator 필드와 매핑됨.
    private List<Incident> incidents = new ArrayList<>(); //그래서 DB에서 조인해서 이 User가 작성한 모든 Incident들을 incidents 리스트로 가져올 수 있음.

    //-----ENUM 클래스
    public enum Role{
        OWNER,
        CREW,
        ADMIN
    }

    //생성자
    //외부 코드에서 new User() 하는 걸 막기 위해 protected 나 private 으로 두는 게 좋음.
    //JPA는 리플렉션으로 접근할 수 있기 때문에 private이어도 문제 없음. -> 리플렉션으로 접근하기 위해선 기본 생성자가 필요하기에 @NoArgsConsturctor있는거
    private User(String email, String passwordHash, String name, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.isApproved = false; //관리자 admin이 승인하면 통과하도록.
    }

    @PrePersist //INSERT 전에 실행
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate //UPDATE 전에 실행.
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ──────────────── 도메인 메서드 (상태 변경) ──────────────── setter인데 이게 필요한가?
    //누가 호출할 수 있는지는 Spring Security + Service 계층에서 컨트롤
    public void approve() {
        this.isApproved = true;
    }

    public void changeName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        this.name = newName;
    }

    public void changePasswordHash(String newHash) {
        if (newHash == null || newHash.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        this.passwordHash = newHash;
    }
}
