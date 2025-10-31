package com.CitaSalud.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA que representa un usuario del sistema.
 *
 * Mapea la tabla "usuario" y contiene los atributos mínimos necesarios para la
 * autenticación y administración del usuario en la aplicación.
 *
 * Notas importantes:
 * - La contraseña se almacena en la base de datos de forma hasheada; nunca almacenar
 *   contraseñas en texto plano. El hashing/validación debe realizarse mediante
 *   PasswordEncoder en los servicios correspondientes.
 * - Se utiliza Lombok (@Getter, @Setter, @NoArgsConstructor) para reducir
 *   código boilerplate; revisar generación de constructores/constructores personalizados
 *   según necesidades de deserialización o pruebas.
 * - La relación roles es ManyToMany. Se ha configurado FetchType.EAGER para
 *   facilitar la autorización en capas superiores; evaluar impacto en rendimiento.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    /**
     * Identificador primario de la entidad (clave autogenerada).
     *
     * Mapeado a la columna "id_usuario".
     */
    @Id //PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    /**
     * Nombre(s) del usuario.
     *
     * Campo requerido.
     */
    @Column(name = "nombre", nullable = false)
    private String nombre;

    /**
     * Apellido(s) del usuario.
     *
     * Campo requerido.
     */
    @Column(name = "apellido", nullable = false)
    private String apellido;

    /**
     * Tipo de documento de identidad (por ejemplo, "CC", "TI").
     *
     * Campo requerido.
     */
    @Column(name = "tipo_doc", nullable = false)
    private String tipoDocumento;

    /**
     * Número del documento de identidad.
     *
     * Campo requerido; debería ser único en la lógica de negocio (validar en capa de repositorio/servicio).
     */
    @Column(name = "num_doc", nullable = false)
    private String numeroDocumento;

    /**
     * Correo electrónico del usuario.
     *
     * Campo requerido; se utiliza para autenticación y comunicación. Debe validarse su formato
     * y, preferiblemente, su unicidad en la base de datos.
     */
    @Column(name = "email", nullable = false)
    private String email;

    /**
     * Teléfono de contacto.
     *
     * Campo requerido; formato y validez deben gestionarse en capas superiores.
     */
    @Column(name = "telefono", nullable = false)
    private String telefono;

    /**
     * Hash de la contraseña del usuario.
     *
     * IMPORTANTE: almacenar siempre el hash (BCrypt u otro algoritmo seguro), y nunca la contraseña
     * en texto plano. Su verificación debe realizarse mediante PasswordEncoder.matches(...).
     */
    @Column(name = "contrasena", nullable = false)
    private String contrasena;

    /**
     * Fecha de nacimiento del usuario.
     *
     * Campo requerido; usar LocalDate para evitar problemas de zona horaria.
     */
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    /**
     * Roles asociados al usuario.
     *
     * - Relación ManyToMany con la entidad Rol.
     * - Tabla intermedia: "usuario_rol".
     * - FetchType.EAGER para disponer de los roles inmediatamente (necesario para autorización),
     *   evaluar si es preferible LAZY y cargarlos explícitamente en ciertos flujos.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_rol",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_rol"))
    private Set<Rol> roles = new HashSet<>();

}
