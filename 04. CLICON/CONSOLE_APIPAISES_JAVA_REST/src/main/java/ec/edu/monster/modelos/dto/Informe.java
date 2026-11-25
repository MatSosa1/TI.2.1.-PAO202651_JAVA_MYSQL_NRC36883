package ec.edu.monster.modelos.dto;

import java.util.List;

/** Los tres informes estadísticos de GET /api/Reportes (ECUD7). */
public record Informe(List<ConteoRegion> paisesPorRegion, Pais paisMasPoblado, List<PromedioRegion> promedioPorRegion) {

  /** Cantidad de países de una región. */
  public record ConteoRegion(String region, int cantidad) {
  }

  /** Promedio de población de una región. */
  public record PromedioRegion(String region, long promedioPoblacion) {
  }
}
