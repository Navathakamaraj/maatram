package org.maatram.repository;

import org.maatram.model.AlumniRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlumniRegistrationRepository extends JpaRepository<AlumniRegistration, Long> {
}
