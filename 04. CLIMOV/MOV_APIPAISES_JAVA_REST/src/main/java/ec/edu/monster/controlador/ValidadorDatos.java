package ec.edu.monster.controlador;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Validación en el cliente antes de enviar (ERS 9.1.1): usuario y clave no vacíos, nombre de país
 * no vacío y de hasta 100 caracteres, región elegida de la lista. El servidor repite las reglas.
 */
public final class ValidadorDatos {

    public static final int MAX_NOMBRE = 100;

    public static final String INGRESE_USUARIO = "Ingrese el usuario";
    public static final String INGRESE_CLAVE = "Ingrese la clave";
    public static final String INGRESE_NOMBRE_PAIS = "Ingrese el nombre del país";
    public static final String NOMBRE_MUY_LARGO = "El nombre no puede superar 100 caracteres";

    /** Regiones válidas (ECUD, parámetros generales), en el orden del catálogo. */
    public static final List<String> REGIONES = Collections.unmodifiableList(
            Arrays.asList("América", "Europa", "Asia", "África", "Oceanía", "Antártida"));

    private ValidadorDatos() {
    }

    public static String limpiar(String texto) {
        return texto != null ? texto.trim() : "";
    }

    /** @return el mensaje de error, o null si el nombre es válido (ECUD3, alterno 3). */
    public static String errorNombre(String nombre) {
        String limpio = limpiar(nombre);
        if (limpio.isEmpty()) {
            return INGRESE_NOMBRE_PAIS;
        }
        return limpio.length() > MAX_NOMBRE ? NOMBRE_MUY_LARGO : null;
    }

    /** Criterio de búsqueda opcional: vacío vale; con texto, hasta 100 caracteres. */
    public static String errorCriterio(String criterio) {
        return limpiar(criterio).length() > MAX_NOMBRE ? NOMBRE_MUY_LARGO : null;
    }
}
