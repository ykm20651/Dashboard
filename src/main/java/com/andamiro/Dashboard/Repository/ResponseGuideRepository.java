package com.andamiro.Dashboard.Repository;

import com.andamiro.Dashboard.Entity.ResponseGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResponseGuideRepository extends JpaRepository<ResponseGuide, UUID> {
}
