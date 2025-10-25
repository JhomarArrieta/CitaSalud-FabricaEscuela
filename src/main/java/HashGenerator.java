import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase utilitaria para generar hashes BCrypt de contraseñas.
 * 
 * Esta NO es parte de la aplicación principal, sino una herramienta de desarrollo
 * para generar contraseñas hasheadas que se pueden insertar directamente en la
 * base de datos durante pruebas o inicialización de datos.
 * 
 * USO:
 * 1. Editar el array 'plain' con las contraseñas que quieres hashear
 * 2. Ejecutar esta clase como aplicación Java normal (main method)
 * 3. Copiar los hashes generados e insertarlos en tu base de datos
 * 
 * Ejemplo de salida:
 * password -> $2a$12$KIX8qF7x8yF.BoHGqXQwxe5fP8wZ9K...
 * admin123 -> $2a$12$vZKZfP3yW.PoGnFqAXSwde7gR9aB8L...
 */
public class HashGenerator {
    
    public static void main(String[] args) {
        // Strength 12 => Factor de coste BCrypt (más alto = más seguro pero más lento)
        // Valores comunes: 10-14
        // 12 es un buen balance entre seguridad y rendimiento
        PasswordEncoder encoder = new BCryptPasswordEncoder(12);

        // Array de contraseñas en texto plano para hashear
        // Puedes editarlas aquí o pasarlas como argumentos del programa
        String[] plain = new String[] {
                "password",      // Contraseña simple para usuario de prueba
                "admin123",      // Contraseña para administrador
                "doctor2025"     // Contraseña para médico
        };

        // Iterar sobre cada contraseña y generar su hash
        for (String p : plain) {
            String hash = encoder.encode(p);
            // Imprimir el resultado en formato: contraseña -> hash
            System.out.println(p + " -> " + hash);
        }
        
        // Nota importante sobre BCrypt:
        // - Cada vez que ejecutes este programa, los hashes serán DIFERENTES
        // - Esto es normal y esperado (BCrypt genera un salt aleatorio cada vez)
        // - Todos los hashes generados son válidos y funcionarán correctamente
        // - La validación se hace con encoder.matches(plain, hash)
    }
}
