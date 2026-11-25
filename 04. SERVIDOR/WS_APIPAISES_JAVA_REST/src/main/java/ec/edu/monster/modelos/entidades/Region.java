package ec.edu.monster.modelos.entidades;

/** Fila del catálogo Regiones (América, Europa, Asia, África, Oceanía, Antártida). */
public class Region {

    private int idRegion;
    private String nombre;

    public Region() {
    }

    public Region(int idRegion, String nombre) {
        this.idRegion = idRegion;
        this.nombre = nombre;
    }

    public int getIdRegion() {
        return idRegion;
    }

    public void setIdRegion(int idRegion) {
        this.idRegion = idRegion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
