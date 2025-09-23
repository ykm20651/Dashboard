package com.andamiro.Dashboard.Repository;

import com.andamiro.Dashboard.Entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    List<Incident> findByCreatorId(UUID creatorId);
    //SELECT * FROM incidents WHERE creator_id = ? 쿼리가 자동으로 생성돼.
    /*
    Q) 이거 DB에 컬럼명이랑 일치하는거 자동 변환하는거 어디서 흐름타고 이렇게 되는거임?


     */

}
