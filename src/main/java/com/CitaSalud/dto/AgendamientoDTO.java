package com.CitaSalud.dto;

import java.time.LocalDateTime;

// No necesita anotaciones de Spring o JPA, es solo un contenedor de datos.
public class AgendamientoDTO {

    private Long usuarioId;
    private Integer sedeId;
    private Integer examenId;
    private LocalDateTime fechaHora;

    public AgendamientoDTO() {
    }

    public AgendamientoDTO(Long usuarioId, Integer sedeId, Integer examenId, LocalDateTime fechaHora) {
        this.usuarioId = usuarioId;
        this.sedeId = sedeId;
        this.examenId = examenId;
        this.fechaHora = fechaHora;
    }

    // Getters y Setters.

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Integer getSedeId() {
        return sedeId;
    }

    public void setSedeId(Integer sedeId) {
        this.sedeId = sedeId;
    }

    public Integer getExamenId() {
        return examenId;
    }

    public void setExamenId(Integer examenId) {
        this.examenId = examenId;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}
