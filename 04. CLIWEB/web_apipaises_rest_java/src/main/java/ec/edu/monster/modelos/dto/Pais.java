package ec.edu.monster.modelos.dto;

/**
 * País guardado o consultado en la API, con los campos del JSON del servidor (Gson los llena).
 * {@code guardado} solo llega en la consulta a la API (ECUD3, paso 6).
 */
public class Pais {

    private String nombre;
    private String capital;
    private String region;
    private long poblacion;
    private String moneda;
    private boolean guardado;

    public Pais() {
    }

    public Pais(String nombre, String capital, String region, long poblacion, String moneda) {
        this.nombre = nombre;
        this.capital = capital;
        this.region = region;
        this.poblacion = poblacion;
        this.moneda = moneda;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCapital() {
        return capital;
    }

    public String getRegion() {
        return region;
    }

    public long getPoblacion() {
        return poblacion;
    }

    /** Población con separador de miles (ECUD, datos del país). */
    public String getPoblacionFormateada() {
        return Formatos.poblacion(poblacion);
    }

    public String getMoneda() {
        return moneda;
    }

    public boolean isGuardado() {
        return guardado;
    }

    public void setGuardado(boolean guardado) {
        this.guardado = guardado;
    }
}
