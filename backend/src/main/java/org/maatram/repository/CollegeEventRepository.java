package org.maatram.repository;

import org.maatram.model.CollegeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollegeEventRepository extends JpaRepository<CollegeEvent, Long> {
}
