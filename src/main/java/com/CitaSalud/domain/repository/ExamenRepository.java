package com.CitaSalud.domain.repository;

import com.CitaSalud.domain.entities.Examen;
import com.CitaSalud.domain.entities.TipoExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamenRepository extends JpaRepository<Examen, Long> {

    List<Examen> findByTipoExamen(TipoExamen tipoExamen);

    List<Examen> findByNombreContainingIgnoreCase(String nombre);

}
