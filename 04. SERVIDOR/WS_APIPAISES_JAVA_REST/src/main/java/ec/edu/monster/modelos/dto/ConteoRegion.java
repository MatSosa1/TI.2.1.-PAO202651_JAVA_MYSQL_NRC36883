package ec.edu.monster.modelos.dto;

/** Cantidad de países de una región (ECUD7, paso 4). */
public class ConteoRegion {

    private String region;
    private int cantidad;

    public ConteoRegion() {
    }

    public ConteoRegion(String region, int cantidad) {
        this.region = region;
        this.cantidad = cantidad;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
