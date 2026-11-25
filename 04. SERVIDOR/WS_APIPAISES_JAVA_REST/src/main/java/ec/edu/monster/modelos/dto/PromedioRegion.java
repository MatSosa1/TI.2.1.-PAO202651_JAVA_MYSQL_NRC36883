package ec.edu.monster.modelos.dto;

/** Promedio de población de una región, redondeado a entero; 0 si no tiene países (ECUD7, paso 6). */
public class PromedioRegion {

    private String region;
    private long promedioPoblacion;

    public PromedioRegion() {
    }

    public PromedioRegion(String region, long promedioPoblacion) {
        this.region = region;
        this.promedioPoblacion = promedioPoblacion;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public long getPromedioPoblacion() {
        return promedioPoblacion;
    }

    public void setPromedioPoblacion(long promedioPoblacion) {
        this.promedioPoblacion = promedioPoblacion;
    }
}
