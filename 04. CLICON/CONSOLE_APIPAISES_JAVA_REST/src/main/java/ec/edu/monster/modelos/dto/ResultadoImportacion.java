package ec.edu.monster.modelos.dto;

/** País importado y si fue «registrado» o «actualizado» (ECUD2, paso 8). */
public record ResultadoImportacion(Pais pais, String accion) {
}
