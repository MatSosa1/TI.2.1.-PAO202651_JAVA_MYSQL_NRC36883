package ec.edu.monster.modelos.dto;

/** Resultado de «Importar todos»: países recibidos de la API, registrados y actualizados. */
public record ResumenImportacion(int recibidos, int registrados, int actualizados) {
}
