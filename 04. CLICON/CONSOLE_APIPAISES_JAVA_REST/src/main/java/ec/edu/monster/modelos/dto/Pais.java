package ec.edu.monster.modelos.dto;

/**
 * País guardado o consultado en la API. {@code guardado} solo llega en la consulta a la API
 * (ECUD3, paso 6): indica si ya está en la base de datos local.
 */
public record Pais(String nombre, String capital, String region, long poblacion, String moneda, boolean guardado) {
}
