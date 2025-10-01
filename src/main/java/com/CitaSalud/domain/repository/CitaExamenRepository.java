package com.CitaSalud.domain.repository;

import com.CitaSalud.domain.entities.CitaExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CitaExamenRepository extends JpaRepository<CitaExamen, Integer> {
    // Vacío por ahora
}