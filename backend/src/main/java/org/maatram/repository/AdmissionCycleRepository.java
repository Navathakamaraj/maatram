package org.maatram.repository;

import org.maatram.model.AdmissionCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdmissionCycleRepository extends JpaRepository<AdmissionCycle, Long> {
}
