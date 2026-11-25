package ec.edu.monster.servicios;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * SHA-256 en hexadecimal en minúsculas (64 caracteres). Es el mismo valor que produce
 * SHA2(texto, 256) en MySQL, que usa init.sql para guardar las claves.
 */
public class HashUtil {

    public String sha256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // Todo JRE trae SHA-256 (exigido por la especificación de Java)
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    /** Compara el hash de la clave recibida con el guardado, sin distinguir mayúsculas del hex. */
    public boolean coincide(String clave, String hashGuardado) {
        if (clave == null || hashGuardado == null) {
            return false;
        }
        byte[] calculado = sha256(clave).getBytes(StandardCharsets.US_ASCII);
        byte[] guardado = hashGuardado.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII);
        // Comparación en tiempo constante
        return MessageDigest.isEqual(calculado, guardado);
    }
}
