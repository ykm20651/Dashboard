package com.andamiro.Dashboard.Repository;

import com.andamiro.Dashboard.Entity.EvidenceFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EvidenceFileRepository extends JpaRepository<EvidenceFile, UUID> {
}
