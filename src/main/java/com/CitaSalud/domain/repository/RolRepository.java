package com.CitaSalud.domain.repository;

import com.CitaSalud.domain.entities.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Long> {
    // Spring proporciona: save(), findById(), findAll(), delete(), etc.
}
