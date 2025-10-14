import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad para generar hashes BCrypt de contraseñas.
 * Ejecutar con: java GenerateBCryptHashes.java
 */
public class GenerateBCryptHashes {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("=== Generando hashes BCrypt ===");
        System.out.println();

        // Contraseña: admin
        String adminHash = encoder.encode("admin");
        System.out.println("admin -> " + adminHash);

        // Contraseña: manager
        String managerHash = encoder.encode("manager");
        System.out.println("manager -> " + managerHash);

        // Contraseña: user
        String userHash = encoder.encode("user");
        System.out.println("user -> " + userHash);

        System.out.println();
        System.out.println("=== Verificando hashes ===");

        // Verificar que funcionen
        System.out.println("¿'admin' coincide? " + encoder.matches("admin", adminHash));
        System.out.println("¿'manager' coincide? " + encoder.matches("manager", managerHash));
        System.out.println("¿'user' coincide? " + encoder.matches("user", userHash));
    }
}
