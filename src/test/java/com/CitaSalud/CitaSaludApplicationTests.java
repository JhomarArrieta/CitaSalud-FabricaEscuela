package com.CitaSalud;

import com.CitaSalud.core.services.CitaExamenService;
import com.CitaSalud.domain.entities.*; // Importar Sede, Examen y el NUEVO EstadoCita
import com.CitaSalud.domain.repository.CitaExamenRepository;
import com.CitaSalud.domain.repository.DisponibilidadRepository;
import com.CitaSalud.domain.repository.UsuarioRepository;
import com.CitaSalud.dto.AgendamientoDTO;
import com.CitaSalud.dto.CancelacionDTO;
import com.CitaSalud.exceptions.CuposAgotadosException;
import com.CitaSalud.exceptions.RecursoNoEncontradoException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para CitaExamenService.
 * Usamos Mockito para simular (mock) los repositorios y aislar la lógica del servicio.
 */
@ExtendWith(MockitoExtension.class)
class CitaSaludApplicationTests { // Nombre de la clase del archivo del usuario

    @Mock
    private DisponibilidadRepository disponibilidadRepository;

    @Mock
    private CitaExamenRepository citaExamenRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CitaExamenService citaExamenService;

    // Variables de prueba reutilizables
    private Usuario usuarioPrueba;
    private Disponibilidad disponibilidadPrueba; // Mockeada
    private AgendamientoDTO agendamientoDTO;
    private LocalDateTime fechaHoraCita;

    @BeforeEach
    void setUp() {
        fechaHoraCita = LocalDateTime.of(2025, 12, 1, 10, 30);

        usuarioPrueba = new Usuario();
        usuarioPrueba.setIdUsuario(1L);
        usuarioPrueba.setNombre("Paciente Prueba");

        // Mockeamos la entidad para controlar 'ocuparCupo'
        disponibilidadPrueba = mock(Disponibilidad.class);
        // Simular que 'ocuparCupo' funciona sin lanzar excepción
        doNothing().when(disponibilidadPrueba).ocuparCupo();


        agendamientoDTO = new AgendamientoDTO();
        agendamientoDTO.setUsuarioId(1L);
        agendamientoDTO.setSedeId(10L);
        agendamientoDTO.setExamenId(20L);
        agendamientoDTO.setFechaHora(fechaHoraCita);
    }

    // --- Pruebas para agendarExamen ---

    @Test
    void testAgendarExamen_Exitoso() {
        // 1. Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));
        when(disponibilidadRepository.findAndLockForUpdate(
                agendamientoDTO.getSedeId(),
                agendamientoDTO.getExamenId(),
                fechaHoraCita.toLocalDate(),
                fechaHoraCita.toLocalTime()
        )).thenReturn(Optional.of(disponibilidadPrueba));

        when(citaExamenRepository.save(any(CitaExamen.class))).thenAnswer(invocation -> {
            CitaExamen citaGuardada = invocation.getArgument(0);
            citaGuardada.setIdCita(99L);
            return citaGuardada;
        });

        // 2. Act
        CitaExamen citaAgendada = citaExamenService.agendarExamen(agendamientoDTO);

        // 3. Assert
        assertNotNull(citaAgendada);

        // ===================================================
        // --- CORRECCIÓN DE TEST (Historia de Usuario) ---
        // Se valida el Enum, no el String "AGENDADA"
        assertEquals(EstadoCita.CONFIRMADO, citaAgendada.getEstado());
        // ===================================================

        assertEquals(usuarioPrueba, citaAgendada.getUsuario());
        verify(disponibilidadPrueba, times(1)).ocuparCupo();
        verify(disponibilidadRepository, times(1)).save(disponibilidadPrueba);
        verify(citaExamenRepository, times(1)).save(any(CitaExamen.class));
    }

    @Test
    void testAgendarExamen_Falla_UsuarioNoEncontrado() {
        // (Sin cambios, este test estaba bien)
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> citaExamenService.agendarExamen(agendamientoDTO)
        );
        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
        verify(disponibilidadRepository, never()).findAndLockForUpdate(any(), any(), any(), any());
    }

    @Test
    void testAgendarExamen_Falla_CuposAgotados() {
        // (Sin cambios, este test estaba bien)
        // (Renombrado para claridad, antes se llamaba "Falla_DisponibilidadNoEncontrada")
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));
        when(disponibilidadRepository.findAndLockForUpdate(
                agendamientoDTO.getSedeId(),
                agendamientoDTO.getExamenId(),
                fechaHoraCita.toLocalDate(),
                fechaHoraCita.toLocalTime()
        )).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> citaExamenService.agendarExamen(agendamientoDTO)
        );
        assertEquals("No hay disponibilidad para el examen en la sede y fecha seleccionada.", exception.getMessage());
        verify(citaExamenRepository, never()).save(any());
    }

    @Test
    void testAgendarExamen_Falla_OcuparCupo_lanzaExcepcion() {
        // (Sin cambios, este test estaba bien)
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));
        when(disponibilidadRepository.findAndLockForUpdate(any(), any(), any(), any()))
                .thenReturn(Optional.of(disponibilidadPrueba));

        doThrow(new IllegalStateException("Simulación: Cupos llenos"))
                .when(disponibilidadPrueba).ocuparCupo();

        assertThrows(
                CuposAgotadosException.class,
                () -> citaExamenService.agendarExamen(agendamientoDTO)
        );
        verify(disponibilidadRepository, never()).save(disponibilidadPrueba);
        verify(citaExamenRepository, never()).save(any());
    }


    // --- Pruebas para cancelarExamen ---

    @Test
    void testCancelarExamen_Exitoso() {
        // 1. Arrange
        CancelacionDTO cancelacionDTO = new CancelacionDTO();
        cancelacionDTO.setUsuarioId(1L);
        cancelacionDTO.setCitaId(99L);
        cancelacionDTO.setMotivo("Motivo de prueba");

        CitaExamen citaExistente = mock(CitaExamen.class);
        when(citaExistente.getUsuario()).thenReturn(usuarioPrueba);

        // ===================================================
        // --- CORRECCIÓN DE TEST (Historia de Usuario) ---
        // El estado inicial debe ser el Enum
        when(citaExistente.getEstado()).thenReturn(EstadoCita.CONFIRMADO);
        // ===================================================

        when(citaExistente.getDisponibilidad()).thenReturn(disponibilidadPrueba);
        when(citaExistente.getFechaHora()).thenReturn(fechaHoraCita);

        when(disponibilidadPrueba.getSede()).thenReturn(mock(Sede.class));
        when(disponibilidadPrueba.getExamen()).thenReturn(mock(Examen.class));
        doNothing().when(disponibilidadPrueba).liberarCupo();

        when(citaExamenRepository.findById(99L)).thenReturn(Optional.of(citaExistente));
        when(disponibilidadRepository.findAndLockForUpdate(any(), any(), any(), any()))
                .thenReturn(Optional.of(disponibilidadPrueba));

        // 2. Act
        CitaExamen citaCancelada = citaExamenService.cancelarExamen(cancelacionDTO);

        // 3. Assert
        // ===================================================
        assertNotNull(citaCancelada, "La cita cancelada no debe ser null");

        // --- CORRECCIÓN DE TEST (Historia de Usuario) ---
        // Verificar que se llamó a setEstado con el Enum
        verify(citaExistente, times(1)).setEstado(EstadoCita.CANCELADO);
        // ===================================================

        verify(citaExistente, times(1)).setMotivoCancelacion("Motivo de prueba");
        verify(disponibilidadPrueba, times(1)).liberarCupo();
        verify(disponibilidadRepository, times(1)).save(disponibilidadPrueba);
        verify(citaExamenRepository, times(1)).save(citaExistente);
    }


    @Test
    void testCancelarExamen_Falla_CitaNoEncontrada() {
        // (Sin cambios, este test estaba bien)
        CancelacionDTO dto = new CancelacionDTO();
        dto.setUsuarioId(1L);
        dto.setCitaId(99L);
        dto.setMotivo("Motivo");

        when(citaExamenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> citaExamenService.cancelarExamen(dto)
        );
    }

    @Test
    void testCancelarExamen_Falla_UsuarioNoAutorizado() {
        // (Sin cambios en la lógica, solo en la Excepción esperada)
        CancelacionDTO dto = new CancelacionDTO();
        dto.setUsuarioId(1L); // Usuario 1 intenta cancelar
        dto.setCitaId(99L);
        dto.setMotivo("Motivo");

        Usuario otroUsuario = new Usuario();
        otroUsuario.setIdUsuario(2L); // Pero la cita es del Usuario 2

        CitaExamen citaExistente = new CitaExamen();
        citaExistente.setUsuario(otroUsuario);

        when(citaExamenRepository.findById(99L)).thenReturn(Optional.of(citaExistente));

        // ===================================================
        // --- CORRECCIÓN DE TEST (Lógica de Servicio) ---
        // El servicio ahora lanza RecursoNoEncontradoException por seguridad
        // en lugar de SecurityException.
        assertThrows(
                RecursoNoEncontradoException.class,
                () -> citaExamenService.cancelarExamen(dto)
        );
        // ===================================================
    }

    @Test
    void testCancelarExamen_Falla_EstadoNoValido() {
        // (Sin cambios, este test estaba correcto)
        CancelacionDTO dto = new CancelacionDTO();
        dto.setUsuarioId(1L);
        dto.setCitaId(99L);
        dto.setMotivo("Motivo");

        CitaExamen citaFinalizada = new CitaExamen();
        citaFinalizada.setUsuario(usuarioPrueba); // El usuario es correcto

        // ===================================================
        // --- CORRECCIÓN DE TEST (Historia de Usuario) ---
        // Se valida contra el Enum, no contra el String "FINALIZADA"
        citaFinalizada.setEstado(EstadoCita.COMPLETADO);
        // ===================================================

        when(citaExamenRepository.findById(99L)).thenReturn(Optional.of(citaFinalizada));

        assertThrows(
                IllegalStateException.class, // El servicio lanza IllegalStateException
                () -> citaExamenService.cancelarExamen(dto)
        );
    }
}