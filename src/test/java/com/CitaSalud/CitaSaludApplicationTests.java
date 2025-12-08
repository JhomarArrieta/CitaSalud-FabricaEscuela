package com.CitaSalud;

import com.CitaSalud.core.services.CitaExamenService;
import com.CitaSalud.domain.entities.*;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// 'LENIENT' permite definir reglas (whens) en el setUp sin que fallen los tests que no las usan.
@MockitoSettings(strictness = Strictness.LENIENT)
class CitaSaludApplicationTests {

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
    private Disponibilidad disponibilidadPrueba;
    private AgendamientoDTO agendamientoDTO;
    private LocalDateTime fechaHoraCita;

    @BeforeEach
    void setUp() {
        fechaHoraCita = LocalDateTime.of(2025, 12, 1, 10, 30);

        usuarioPrueba = new Usuario();
        usuarioPrueba.setIdUsuario(1L);
        usuarioPrueba.setNombre("Paciente Prueba");

        disponibilidadPrueba = mock(Disponibilidad.class);

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
        doNothing().when(disponibilidadPrueba).ocuparCupo();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));

        // CORRECCIÓN: Usar 'findAndLockDisponibilidad' que es el que usa el servicio
        when(disponibilidadRepository.findAndLockDisponibilidad(
                anyLong(),
                anyLong(),
                any(LocalDate.class),
                any(LocalTime.class)
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
        assertEquals(EstadoCita.CONFIRMADO, citaAgendada.getEstado());

        // Verificamos que se llamó al método correcto
        verify(disponibilidadRepository, times(1)).findAndLockDisponibilidad(anyLong(), anyLong(), any(), any());
        verify(disponibilidadRepository, times(1)).save(disponibilidadPrueba);
    }

    @Test
    void testAgendarExamen_Falla_UsuarioNoEncontrado() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> citaExamenService.agendarExamen(agendamientoDTO)
        );
        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
    }

    @Test
    void testAgendarExamen_Falla_CuposAgotados() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));

        // Simulamos que NO se encuentra disponibilidad (Optional.empty)
        when(disponibilidadRepository.findAndLockForUpdate(
                any(), any(), any(), any()
        )).thenReturn(Optional.empty());

        assertThrows(
                CuposAgotadosException.class,
                () -> citaExamenService.agendarExamen(agendamientoDTO)
        );

        verify(citaExamenRepository, never()).save(any());
    }

    @Test
    void testAgendarExamen_Falla_OcuparCupo_lanzaExcepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));

        // Aquí también usamos matchers flexibles para asegurar que entra
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

        // Preparar mocks de Sede y Examen para evitar NullPointer al hacer .getId()
        Sede sedeMock = mock(Sede.class);
        when(sedeMock.getId()).thenReturn(10L);

        Examen examenMock = mock(Examen.class);
        when(examenMock.getId()).thenReturn(20L);

        // Configurar disponibilidad
        when(disponibilidadPrueba.getSede()).thenReturn(sedeMock);
        when(disponibilidadPrueba.getExamen()).thenReturn(examenMock);
        doNothing().when(disponibilidadPrueba).liberarCupo();

        // Configurar Cita
        CitaExamen citaExistente = mock(CitaExamen.class);
        when(citaExistente.getUsuario()).thenReturn(usuarioPrueba);
        when(citaExistente.getEstado()).thenReturn(EstadoCita.CONFIRMADO);
        when(citaExistente.getDisponibilidad()).thenReturn(disponibilidadPrueba);
        when(citaExistente.getFechaHora()).thenReturn(fechaHoraCita);

        // Mocks de Repositorios
        when(citaExamenRepository.findById(99L)).thenReturn(Optional.of(citaExistente));
        when(citaExamenRepository.save(citaExistente)).thenReturn(citaExistente);

        // CORRECCIÓN: Simular la búsqueda que hace el servicio para bloquear
        when(disponibilidadRepository.findAndLockForUpdate(
                eq(10L),                // Sede ID
                eq(20L),                // Examen ID
                any(LocalDate.class),
                any(LocalTime.class)
        )).thenReturn(Optional.of(disponibilidadPrueba));

        // 2. Act
        CitaExamen citaCancelada = citaExamenService.cancelarExamen(cancelacionDTO);

        // 3. Assert
        assertNotNull(citaCancelada);
        verify(citaExistente).setEstado(EstadoCita.CANCELADA);
        verify(disponibilidadPrueba).liberarCupo();
        verify(disponibilidadRepository).save(disponibilidadPrueba); // Verifica que se guardó la disponibilidad
    }

    @Test
    void testCancelarExamen_Falla_CitaNoEncontrada() {
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
        CancelacionDTO dto = new CancelacionDTO();
        dto.setUsuarioId(1L);
        dto.setCitaId(99L);
        dto.setMotivo("Motivo");

        Usuario otroUsuario = new Usuario();
        otroUsuario.setIdUsuario(2L);

        CitaExamen citaExistente = new CitaExamen();
        citaExistente.setUsuario(otroUsuario);

        when(citaExamenRepository.findById(99L)).thenReturn(Optional.of(citaExistente));

        // Ajustamos la excepción a SecurityException según tu log anterior
        assertThrows(
                SecurityException.class,
                () -> citaExamenService.cancelarExamen(dto)
        );
    }

    @Test
    void testCancelarExamen_Falla_EstadoNoValido() {
        CancelacionDTO dto = new CancelacionDTO();
        dto.setUsuarioId(1L);
        dto.setCitaId(99L);
        dto.setMotivo("Motivo");

        CitaExamen citaFinalizada = new CitaExamen();
        citaFinalizada.setUsuario(usuarioPrueba);
        citaFinalizada.setEstado(EstadoCita.COMPLETADO);

        when(citaExamenRepository.findById(99L)).thenReturn(Optional.of(citaFinalizada));

        assertThrows(
                IllegalStateException.class,
                () -> citaExamenService.cancelarExamen(dto)
        );
    }
}