package com.CitaSalud.domain.repository;

import com.CitaSalud.domain.entities.TipoExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoExamenRepository extends JpaRepository<TipoExamen, Long> {
    TipoExamen findByNombreIgnoreCase(String nombre);
}
