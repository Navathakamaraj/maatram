package org.maatram.repository;

import org.maatram.model.EventCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventCheckpointRepository extends JpaRepository<EventCheckpoint, Long> {
}
