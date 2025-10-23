import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        // Strength 12 => mismo coste que usamos en ejemplos
        PasswordEncoder encoder = new BCryptPasswordEncoder(12);

        // Puedes pasar contraseñas por args o editarlas aquí
        String[] plain = new String[] {
                "password"
        };

        for (String p : plain) {
            String hash = encoder.encode(p);
            System.out.println(p + " -> " + hash);
        }
    }
}
