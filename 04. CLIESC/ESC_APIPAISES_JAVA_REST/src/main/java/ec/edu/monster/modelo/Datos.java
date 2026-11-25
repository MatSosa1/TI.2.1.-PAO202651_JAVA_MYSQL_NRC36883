package ec.edu.monster.modelo;

import java.util.List;

/**
 * Datos que devuelve el servidor, con los mismos nombres de campo que su JSON (Gson los lee
 * directamente en estos records).
 */
public final class Datos {

    private Datos() {
    }

    /** País guardado o consultado; {@code guardado} solo llega en la consulta a la API (ECUD3). */
    public record Pais(String nombre, String capital, String region, long poblacion, String moneda,
            boolean guardado) {

        public Pais comoGuardado() {
            return new Pais(nombre, capital, region, poblacion, moneda, true);
        }
    }

    /** ECUD5 y ECUD6: países encontrados y total. */
    public record ResultadoBusqueda(List<Pais> paises, int total) {
    }

    /** ECUD2: país importado y si fue «registrado» o «actualizado». */
    public record ResultadoImportacion(Pais pais, String accion) {
    }

    /** «Importar todos»: recibidos de la API, registrados y actualizados. */
    public record ResumenImportacion(int recibidos, int registrados, int actualizados) {
    }

    /** ECUD7: los tres informes. */
    public record Informe(List<ConteoRegion> paisesPorRegion, Pais paisMasPoblado,
            List<PromedioRegion> promedioPorRegion) {
    }

    public record ConteoRegion(String region, int cantidad) {
    }

    public record PromedioRegion(String region, long promedioPoblacion) {
    }
}
