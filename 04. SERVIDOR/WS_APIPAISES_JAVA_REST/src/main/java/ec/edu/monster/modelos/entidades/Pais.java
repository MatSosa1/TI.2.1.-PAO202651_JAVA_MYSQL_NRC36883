package ec.edu.monster.modelos.entidades;

import ec.edu.monster.modelos.dto.PaisDTO;
import jakarta.json.bind.annotation.JsonbTransient;

/**
 * Fila de la tabla Paises. {@code region} es el nombre de la región (Regiones.Nombre),
 * que es lo que muestran los clientes; {@code idRegion} es la clave foránea.
 */
public class Pais {

    private String nombre;
    private String capital;
    private int idRegion;
    private String region;
    private long poblacion;
    private String moneda;

    public Pais() {
    }

    public Pais(String nombre, String capital, String region, long poblacion, String moneda) {
        this.nombre = nombre;
        this.capital = capital;
        this.region = region;
        this.poblacion = poblacion;
        this.moneda = moneda;
    }

    /** País a guardar a partir de lo que devolvió la API. */
    public static Pais desde(PaisDTO dto) {
        return new Pais(dto.getNombre(), dto.getCapital(), dto.getRegion(), dto.getPoblacion(), dto.getMoneda());
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

    /** Dato interno de la base: los clientes reciben el nombre de la región, no su id. */
    @JsonbTransient
    public int getIdRegion() {
        return idRegion;
    }

    public void setIdRegion(int idRegion) {
        this.idRegion = idRegion;
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
}
