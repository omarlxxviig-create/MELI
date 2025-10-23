import java.security.SecureRandom;
import java.util.Base64;

public class GenerateSecureKey {
    public static void main(String[] args) {
        // Generar clave segura de 512 bits (64 bytes) para HS512
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[64]; // 64 bytes * 8 = 512 bits
        random.nextBytes(keyBytes);
        String encodedKey = Base64.getEncoder().encodeToString(keyBytes);

        System.out.println("Clave segura generada para HS512 (512 bits):");
        System.out.println(encodedKey);
        System.out.println("Longitud: " + encodedKey.length() + " caracteres");
        System.out.println("Tamaño: " + (encodedKey.length() * 6) + " bits aproximadamente");

        System.out.println("\nAgrega esta clave a tu archivo application.yml:");
        System.out.println("jwt:");
        System.out.println("  secret: \"" + encodedKey + "\"");
    }
}
