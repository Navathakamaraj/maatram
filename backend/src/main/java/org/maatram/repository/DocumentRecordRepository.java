package org.maatram.repository;

import org.maatram.model.DocumentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRecordRepository extends JpaRepository<DocumentRecord, Long> {
}
