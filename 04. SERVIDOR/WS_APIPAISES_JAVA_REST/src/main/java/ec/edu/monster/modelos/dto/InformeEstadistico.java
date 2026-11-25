package ec.edu.monster.modelos.dto;

import ec.edu.monster.modelos.entidades.Pais;
import java.util.List;

/** Los tres informes de GET /api/Reportes (RQF-007, ECUD7). */
public class InformeEstadistico {

    private List<ConteoRegion> paisesPorRegion;
    private Pais paisMasPoblado;
    private List<PromedioRegion> promedioPorRegion;

    public InformeEstadistico() {
    }

    public InformeEstadistico(List<ConteoRegion> paisesPorRegion, Pais paisMasPoblado,
            List<PromedioRegion> promedioPorRegion) {
        this.paisesPorRegion = paisesPorRegion;
        this.paisMasPoblado = paisMasPoblado;
        this.promedioPorRegion = promedioPorRegion;
    }

    public List<ConteoRegion> getPaisesPorRegion() {
        return paisesPorRegion;
    }

    public void setPaisesPorRegion(List<ConteoRegion> paisesPorRegion) {
        this.paisesPorRegion = paisesPorRegion;
    }

    public Pais getPaisMasPoblado() {
        return paisMasPoblado;
    }

    public void setPaisMasPoblado(Pais paisMasPoblado) {
        this.paisMasPoblado = paisMasPoblado;
    }

    public List<PromedioRegion> getPromedioPorRegion() {
        return promedioPorRegion;
    }

    public void setPromedioPorRegion(List<PromedioRegion> promedioPorRegion) {
        this.promedioPorRegion = promedioPorRegion;
    }
}
