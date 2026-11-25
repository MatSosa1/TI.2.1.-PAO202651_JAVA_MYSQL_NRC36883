package ec.edu.monster.modelos.dto;

/**
 * País tal como llega de la API REST Countries, ya convertido (ECUD9).
 * {@code guardado} indica si ya existe en la base de datos local (ECUD3, paso 6).
 */
public class PaisDTO {

    private String nombre;
    private String capital;
    private String region;
    private long poblacion;
    private String moneda;
    private boolean guardado;

    public PaisDTO() {
    }

    public PaisDTO(String nombre, String capital, String region, long poblacion, String moneda) {
        this.nombre = nombre;
        this.capital = capital;
        this.region = region;
        this.poblacion = poblacion;
        this.moneda = moneda;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public long getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(long poblacion) {
        this.poblacion = poblacion;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public boolean isGuardado() {
        return guardado;
    }

    public void setGuardado(boolean guardado) {
        this.guardado = guardado;
    }
}
