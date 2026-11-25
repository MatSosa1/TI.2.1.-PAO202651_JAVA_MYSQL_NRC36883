package ec.edu.monster.modelos.servicios;

import com.google.gson.reflect.TypeToken;
import ec.edu.monster.modelos.dto.Pais;
import ec.edu.monster.modelos.dto.Resultados;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/** Países en el servidor: ECUD2 a ECUD6. Única clase que conoce /api/Paises. */
public class PaisService {

    private static final Type LISTA_PAISES = new TypeToken<List<Pais>>() { }.getType();

    private final ClienteRest cliente;

    public PaisService() {
        this(new ClienteRest());
    }

    public PaisService(ClienteRest cliente) {
        this.cliente = cliente;
    }

    /** ECUD4 Listar. */
    public Resultado<List<Pais>> listar(String token) throws ServicioException {
        return cliente.enviar("GET", "/Paises", token, null, LISTA_PAISES, ClienteRest.ESPERA);
    }

    /** ECUD5 y ECUD6: los criterios vacíos se omiten. */
    public Resultado<Resultados.Busqueda> buscar(String token, String nombre, String capital, String region)
            throws ServicioException {
        StringBuilder consulta = new StringBuilder();
        agregar(consulta, "nombre", nombre);
        agregar(consulta, "capital", capital);
        agregar(consulta, "region", region);
        return cliente.enviar("GET", "/Paises/buscar" + consulta, token, null, Resultados.Busqueda.class,
                ClienteRest.ESPERA);
    }

    /** ECUD3: países de la API, marcados si ya están guardados. */
    public Resultado<List<Pais>> consultarApi(String token, String nombre) throws ServicioException {
        return cliente.enviar("GET", "/Paises/externo/" + ClienteRest.codificar(nombre), token, null,
                LISTA_PAISES, ClienteRest.ESPERA);
    }

    /** ECUD2: guarda el país elegido de la consulta (lo registra o lo actualiza). */
    public Resultado<Resultados.Importacion> importar(String token, String consulta, String nombre)
            throws ServicioException {
        return cliente.enviar("POST", "/Paises/importar", token, solicitud(consulta, nombre),
                Resultados.Importacion.class, ClienteRest.ESPERA);
    }

    public Resultado<Resultados.Resumen> importarTodos(String token) throws ServicioException {
        return cliente.enviar("POST", "/Paises/importar/todos", token, null, Resultados.Resumen.class,
                ClienteRest.ESPERA_IMPORTACION);
    }

    /** ECUD4 Registrar (409 si ya existe). */
    public Resultado<Pais> registrar(String token, String consulta, String nombre) throws ServicioException {
        return cliente.enviar("POST", "/Paises", token, solicitud(consulta, nombre), Pais.class, ClienteRest.ESPERA);
    }

    /** ECUD4 Actualizar: el servidor vuelve a consultar la API. */
    public Resultado<Pais> actualizar(String token, String nombre) throws ServicioException {
        return cliente.enviar("PUT", "/Paises/" + ClienteRest.codificar(nombre), token, null, Pais.class,
                ClienteRest.ESPERA);
    }

    /** ECUD4 Eliminar. */
    public Resultado<Object> eliminar(String token, String nombre) throws ServicioException {
        return cliente.enviar("DELETE", "/Paises/" + ClienteRest.codificar(nombre), token, null, Object.class,
                ClienteRest.ESPERA);
    }

    private static Map<String, String> solicitud(String consulta, String nombre) {
        return Map.of("consulta", consulta == null ? "" : consulta.trim(), "nombre", nombre.trim());
    }

    private static void agregar(StringBuilder consulta, String clave, String valor) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        consulta.append(consulta.isEmpty() ? '?' : '&').append(clave).append('=').append(ClienteRest.codificar(valor));
    }
}
