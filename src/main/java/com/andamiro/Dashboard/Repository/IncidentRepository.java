package com.andamiro.Dashboard.Repository;

import com.andamiro.Dashboard.Entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {


    List<Incident> findByCreatorId(UUID creatorId);
    //SELECT * FROM incidents WHERE creator_id = ? 쿼리가 자동으로 생성돼.
    /*
    Q) 이거 DB에 컬럼명이랑 일치하는거 자동 변환하는거 어디서 흐름타고 이렇게 되는거임?

    findByCreatorId → JPA는 여기서 Creator라는 필드명을 보고, Incident 엔티티 안의 creator 필드를 찾아감.
    creator는 @ManyToOne User로 매핑돼 있을 거야.

    그럼 JPA가 자동으로 이 User 엔티티의 id 컬럼을 참조하는 걸 파악하고
    → SELECT * FROM incidents WHERE creator_id = ? 쿼리를 만들어 실행하는 거지.

    즉, 메서드 이름 → 엔티티 필드 매핑 → DB 컬럼명 매핑 순서로 흘러가.
     */
    @Query("SELECT i FROM Incident i " +
            "LEFT JOIN FETCH i.evidenceFiles " +
            "LEFT JOIN FETCH i.reports " +
            "WHERE i.id = :id")
    Optional<Incident> findByIdWithDetails(@Param("id") UUID id);
}
