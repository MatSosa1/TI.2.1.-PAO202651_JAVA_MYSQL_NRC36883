package ec.edu.monster.modelo;

import ec.edu.monster.modelo.Datos.Informe;

/** Informes estadísticos (ECUD7). */
public interface ReporteServicio {

    Resultado<Informe> generar(String token) throws ServicioException;
}
