package ec.edu.monster.modelos.servicios;

import ec.edu.monster.modelos.dto.Informe;

/** Informes estadísticos (ECUD7). Única clase que conoce /api/Reportes. */
public class ReporteService {

    private final ClienteRest cliente;

    public ReporteService() {
        this(new ClienteRest());
    }

    public ReporteService(ClienteRest cliente) {
        this.cliente = cliente;
    }

    public Resultado<Informe> generar(String token) throws ServicioException {
        return cliente.enviar("GET", "/Reportes", token, null, Informe.class, ClienteRest.ESPERA);
    }
}
