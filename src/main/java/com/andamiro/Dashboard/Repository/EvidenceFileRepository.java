package com.andamiro.Dashboard.Repository;

import com.andamiro.Dashboard.Entity.EvidenceFile;
import com.andamiro.Dashboard.Entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvidenceFileRepository extends JpaRepository<EvidenceFile, UUID> {
    List<EvidenceFile> findByIncidentAndIsDeletedFalse(Incident incident);
}
