package ec.edu.monster.modelo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/** Formatos y reglas de entrada compartidos por las pantallas. */
public final class Formatos {

    public static final int MAX_NOMBRE = 100;

    /** Regiones válidas (ECUD, parámetros generales), en el orden del catálogo. */
    public static final List<String> REGIONES = List.of("América", "Europa", "Asia", "África", "Oceanía", "Antártida");
    public static final String TODAS = "Todas";

    public static final String INGRESE_USUARIO = "Ingrese el usuario";
    public static final String INGRESE_CLAVE = "Ingrese la clave";
    public static final String INGRESE_NOMBRE_PAIS = "Ingrese el nombre del país";
    public static final String NOMBRE_MUY_LARGO = "El nombre no puede superar 100 caracteres";

    private Formatos() {
    }

    /** 18001000 → «18.001.000» (la población se muestra con separador de miles). */
    public static String poblacion(long valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", simbolos).format(valor);
    }

    /** «1 país encontrado», «3 países encontrados» (ECUD5, paso 8). */
    public static String total(int cantidad) {
        return cantidad == 1 ? "1 país encontrado" : cantidad + " países encontrados";
    }

    /** @return el mensaje de error, o null si el nombre es válido (ECUD3, alterno 3). */
    public static String errorNombre(String nombre) {
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty()) {
            return INGRESE_NOMBRE_PAIS;
        }
        return limpio.length() > MAX_NOMBRE ? NOMBRE_MUY_LARGO : null;
    }

    /** «Todas» o vacío significa sin filtro de región. */
    public static String region(String seleccion) {
        return seleccion == null || seleccion.isBlank() || TODAS.equals(seleccion) ? "" : seleccion;
    }
}
