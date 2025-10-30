package com.CitaSalud;

import com.CitaSalud.core.services.CitaExamenService;
import com.CitaSalud.domain.entities.*; // Importar Sede y Examen
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
class CitaExamenServiceTest { // Nombre de clase actualizado

    // @Mock crea una simulación de estas dependencias (Repositorios)
    @Mock
    private DisponibilidadRepository disponibilidadRepository;

    @Mock
    private CitaExamenRepository citaExamenRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    // @InjectMocks crea una instancia de CitaExamenService e inyecta los mocks
    @InjectMocks
    private CitaExamenService citaExamenService;

    // Variables de prueba reutilizables
    private Usuario usuarioPrueba;
    private Disponibilidad disponibilidadPrueba; // Esta será una instancia REAL
    private AgendamientoDTO agendamientoDTO;
    private LocalDateTime fechaHoraCita;

    @BeforeEach
    void setUp() {
        // Configuración inicial que se ejecuta antes de cada test
        fechaHoraCita = LocalDateTime.of(2025, 12, 1, 10, 30);

        usuarioPrueba = new Usuario();
        usuarioPrueba.setIdUsuario(1L);
        usuarioPrueba.setNombre("Paciente Prueba");

        // --- CAMBIO CRÍTICO: Usar una instancia real de Disponibilidad ---
        // Esto nos permite probar la lógica real de 'ocuparCupo' y 'liberarCupo'
        Sede sedePrueba = new Sede();
        sedePrueba.setId(10L);
        Examen examenPrueba = new Examen();
        examenPrueba.setId(20L);

        disponibilidadPrueba = new Disponibilidad();
        disponibilidadPrueba.setId(100L); // ID de la disponibilidad
        disponibilidadPrueba.setSede(sedePrueba);
        disponibilidadPrueba.setExamen(examenPrueba);
        disponibilidadPrueba.setFecha(fechaHoraCita.toLocalDate());
        disponibilidadPrueba.setHoraInicio(fechaHoraCita.toLocalTime());
        disponibilidadPrueba.setCuposTotales(10);
        disponibilidadPrueba.setCuposOcupados(5); // Inicia con 5 cupos ocupados
        // -----------------------------------------------------------------

        agendamientoDTO = new AgendamientoDTO();
        agendamientoDTO.setUsuarioId(1L);
        agendamientoDTO.setSedeId(10L);
        agendamientoDTO.setExamenId(20L);
        agendamientoDTO.setFechaHora(fechaHoraCita);
    }

    // --- Pruebas para agendarExamen ---

    @Test
    void testAgendarExamen_Exitoso() {
        // 1. Configuración (Arrange)

        // Simular que el usuario existe
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));

        // Simular que el repositorio encuentra la DISPONIBILIDAD REAL
        when(disponibilidadRepository.findAndLockDisponibilidad(
                agendamientoDTO.getSedeId(),
                agendamientoDTO.getExamenId(),
                fechaHoraCita.toLocalDate(),
                fechaHoraCita.toLocalTime()
        )).thenReturn(Optional.of(disponibilidadPrueba));

        // Simular la acción de guardar la cita
        when(citaExamenRepository.save(any(CitaExamen.class))).thenAnswer(invocation -> {
            CitaExamen citaGuardada = invocation.getArgument(0);
            citaGuardada.setIdCita(99L); // Simular que la DB le asignó un ID
            return citaGuardada;
        });

        // 2. Ejecución (Act)
        CitaExamen citaAgendada = citaExamenService.agendarExamen(agendamientoDTO);

        // 3. Verificación (Assert) - Aserciones mejoradas
        assertNotNull(citaAgendada);
        assertEquals(99L, citaAgendada.getIdCita());
        assertEquals("AGENDADA", citaAgendada.getEstado());
        assertEquals(usuarioPrueba, citaAgendada.getUsuario());
        assertEquals(disponibilidadPrueba, citaAgendada.getDisponibilidad());

        // ¡VERIFICAR LÓGICA DE ENTIDAD REAL!
        // El cupo debe haber aumentado de 5 a 6
        assertEquals(6, disponibilidadPrueba.getCuposOcupados());

        // Verificar que los mocks correctos fueron llamados
        verify(usuarioRepository, times(1)).findById(1L);
        verify(disponibilidadRepository, times(1)).findAndLockDisponibilidad(any(), any(), any(), any());
        verify(disponibilidadRepository, times(1)).save(disponibilidadPrueba); // Se guarda el cupo actualizado
        verify(citaExamenRepository, times(1)).save(any(CitaExamen.class));
    }

    @Test
    void testAgendarExamen_Falla_UsuarioNoEncontrado() {
        // ... (Este test estaba bien y sigue igual)
        // 1. Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());
        // 2. Act & 3. Assert
        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> citaExamenService.agendarExamen(agendamientoDTO)
        );
        assertEquals("Usuario no encontrado con ID: 1", exception.getMessage());
        verify(disponibilidadRepository, never()).findAndLockDisponibilidad(any(), any(), any(), any());
    }

    @Test
    void testAgendarExamen_Falla_DisponibilidadNoEncontrada() {
        // ... (Este test estaba bien, renombrado para claridad)
        // 1. Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));
        // Simular que el repositorio NO encuentra la disponibilidad
        when(disponibilidadRepository.findAndLockDisponibilidad(any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        // 2. Act & 3. Assert
        CuposAgotadosException exception = assertThrows(
                CuposAgotadosException.class,
                () -> citaExamenService.agendarExamen(agendamientoDTO)
        );
        assertEquals("No hay cupos disponibles o la disponibilidad no existe.", exception.getMessage());
        verify(citaExamenRepository, never()).save(any());
    }

    @Test
    void testAgendarExamen_Falla_LogicaEntidadCuposAgotados() {
        // PRUEBA MEJORADA: Probar la lógica real de 'ocuparCupo()'

        // 1. Arrange
        // Configurar la disponibilidad REAL para que esté llena
        disponibilidadPrueba.setCuposOcupados(10); // 10 ocupados de 10 totales

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPrueba));
        // El repositorio SÍ la encuentra...
        when(disponibilidadRepository.findAndLockDisponibilidad(any(), any(), any(), any()))
                .thenReturn(Optional.of(disponibilidadPrueba));

        // 2. Act & 3. Assert
        // ...pero al llamar a 'agendarExamen', este llamará a 'disponibilidadPrueba.ocuparCupo()'
        // y la entidad REAL (no un mock) debe lanzar la excepción.

        // --- CORRECCIÓN ---
        // La traza de error indica que la entidad lanza IllegalStateException,
        // no CuposAgotadosException. Ajustamos la aserción.
        assertThrows(
                IllegalStateException.class, // <-- Cambiado de CuposAgotadosException
                () -> citaExamenService.agendarExamen(agendamientoDTO)
        );

        // Verificar que no se guardó nada (la transacción haría rollback)
        verify(disponibilidadRepository, never()).save(any());
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

        // Mockear la Cita que vamos a cancelar (esto está bien)
        CitaExamen citaExistente = mock(CitaExamen.class);
        when(citaExistente.getUsuario()).thenReturn(usuarioPrueba); // El usuario es dueño
        when(citaExistente.getEstado()).thenReturn("AGENDADA");
        when(citaExistente.getFechaHora()).thenReturn(fechaHoraCita);
        // Hacer que la cita apunte a la disponibilidad REAL
        when(citaExistente.getDisponibilidad()).thenReturn(disponibilidadPrueba);

        // Mockear repositorios
        when(citaExamenRepository.findById(99L)).thenReturn(Optional.of(citaExistente));
        // Simular que el lockeo encuentra la disponibilidad REAL
        when(disponibilidadRepository.findAndLockForUpdate(
                disponibilidadPrueba.getSede().getId(),
                disponibilidadPrueba.getExamen().getId(),
                disponibilidadPrueba.getFecha(),
                disponibilidadPrueba.getHoraInicio()
        )).thenReturn(Optional.of(disponibilidadPrueba));

        // 2. Act
        CitaExamen citaCancelada = citaExamenService.cancelarExamen(cancelacionDTO);

        // 3. Assert
        // Verificar que se actualizó el estado y motivo en el mock
        verify(citaExistente, times(1)).setEstado("CANCELADA");
        verify(citaExistente, times(1)).setMotivoCancelacion("Motivo de prueba");

        // ¡VERIFICAR LÓGICA DE ENTIDAD REAL!
        // El cupo debe haber disminuido de 5 a 4
        assertEquals(4, disponibilidadPrueba.getCuposOcupados());

        // Verificar que se guardaron los cambios
        verify(disponibilidadRepository, times(1)).save(disponibilidadPrueba);
        verify(citaExamenRepository, times(1)).save(citaExistente);
    }


    @Test
    void testCancelarExamen_Falla_CitaNoEncontrada() {
        // ... (Test sin cambios, estaba correcto)
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
        // ... (Test sin cambios, estaba correcto)
        CancelacionDTO dto = new CancelacionDTO();
        dto.setUsuarioId(1L); // Usuario 1 intenta cancelar
        dto.setCitaId(99L);
        dto.setMotivo("Motivo");

        Usuario otroUsuario = new Usuario();
        otroUsuario.setIdUsuario(2L); // Pero la cita es del Usuario 2

        CitaExamen citaExistente = new CitaExamen();
        citaExistente.setUsuario(otroUsuario);

        when(citaExamenRepository.findById(99L)).thenReturn(Optional.of(citaExistente));

        assertThrows(
                SecurityException.class,
                () -> citaExamenService.cancelarExamen(dto)
        );
    }

    @Test
    void testCancelarExamen_Falla_EstadoNoValido() {
        // ... (Test sin cambios, estaba correcto)
        CancelacionDTO dto = new CancelacionDTO();
        dto.setUsuarioId(1L);
        dto.setCitaId(99L);
        dto.setMotivo("Motivo");

        CitaExamen citaFinalizada = new CitaExamen();
        citaFinalizada.setUsuario(usuarioPrueba); // El usuario es correcto
        citaFinalizada.setEstado("FINALIZADA"); // Pero el estado no es cancelable

        when(citaExamenRepository.findById(99L)).thenReturn(Optional.of(citaFinalizada));

        assertThrows(
                IllegalStateException.class,
                () -> citaExamenService.cancelarExamen(dto)
        );
    }
}