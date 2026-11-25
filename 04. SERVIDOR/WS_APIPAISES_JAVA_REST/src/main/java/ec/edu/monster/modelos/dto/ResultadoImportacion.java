package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.entidades.Pais;

/**
 * Datos de POST /api/Paises/importar: el país guardado y si fue «registrado» o «actualizado»
 * (ECUD2, paso 8).
 */
public class ResultadoImportacion {

    private Pais pais;
    private String accion;

    public ResultadoImportacion() {
    }

    public ResultadoImportacion(Pais pais, String accion) {
        this.pais = pais;
        this.accion = accion;
    }

    public Pais getPais() {
        return pais;
    }

    public void setPais(Pais pais) {
        this.pais = pais;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }
}
