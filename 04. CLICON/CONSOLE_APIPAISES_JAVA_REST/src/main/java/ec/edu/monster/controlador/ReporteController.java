package ec.edu.monster.controlador;

import ec.edu.monster.modelos.data.Endpoints;
import ec.edu.monster.modelos.dto.Informe;
import ec.edu.monster.modelos.dto.Respuesta;
import ec.edu.monster.modelos.http.ClienteRest;

/** Informes estadísticos (ECUD7). */
public class ReporteController {

  public Respuesta<Informe> generar() {
    return ClienteRest.get(Endpoints.REPORTES, Informe.class);
  }
}
