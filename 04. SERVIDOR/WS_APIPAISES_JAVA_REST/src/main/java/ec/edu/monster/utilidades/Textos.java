package ec.edu.monster.utilidades;

import java.text.Normalizer;
import java.util.Locale;

/** Utilidades de texto que comparten el servicio de la API y los DAO. */
public final class Textos {

    /** Longitud máxima de las columnas de texto de Paises. */
    public static final int MAX_LONGITUD = 100;

    private Textos() {
    }

    /**
     * Clave de comparación sin tildes ni mayúsculas. Es la misma regla que aplica la
     * collation utf8mb4_0900_ai_ci de MySQL: «Perú» y «peru» son el mismo país.
     */
    public static String clave(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT);
    }

    public static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    /** Recorta el texto para que quepa en una columna VARCHAR(100). */
    public static String recortar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        return limpio.length() <= MAX_LONGITUD ? limpio : limpio.substring(0, MAX_LONGITUD);
    }
}
