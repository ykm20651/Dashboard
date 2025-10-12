package com.andamiro.Dashboard.Repository;

import com.andamiro.Dashboard.Entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, UUID> {
    Optional<Owner> findByBusinessNumber(String businessNumber);
}
