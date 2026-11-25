package ec.edu.monster.modelos.dto;

/** Resultado de importar todos los países: cuántos llegaron, cuántos se registraron y cuántos se actualizaron. */
public class ResumenImportacion {

    private int recibidos;
    private int registrados;
    private int actualizados;

    public ResumenImportacion() {
    }

    public ResumenImportacion(int recibidos, int registrados, int actualizados) {
        this.recibidos = recibidos;
        this.registrados = registrados;
        this.actualizados = actualizados;
    }

    public int getRecibidos() {
        return recibidos;
    }

    public void setRecibidos(int recibidos) {
        this.recibidos = recibidos;
    }

    public int getRegistrados() {
        return registrados;
    }

    public void setRegistrados(int registrados) {
        this.registrados = registrados;
    }

    public int getActualizados() {
        return actualizados;
    }

    public void setActualizados(int actualizados) {
        this.actualizados = actualizados;
    }
}
