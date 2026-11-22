package org.maatram.repository;

import org.maatram.model.CareerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CareerHistoryRepository extends JpaRepository<CareerHistory, Long> {
}
